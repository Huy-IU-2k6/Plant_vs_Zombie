package pvz.com.entities.Zombies;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

import pvz.com.managers.DesignConfig;
import pvz.com.managers.ScaleManager;

public class ConeheadZombie extends Zombies {

    // Thông số cơ bản
    private static final int BODY_HEALTH = 200;
    private static final int CONE_HEALTH = 370;
    private static final float INITIAL_SPEED = 15f; 

    // Tốc độ Animation (nhanh giống Normal)
    private static final float ANIM_SPEED = 0.055f;

    // Hệ số thu nhỏ Hitbox (để căn hàng cho chuẩn)
    private static final float HITBOX_SCALE = 0.85f;

    // State
    private boolean coneOnHead = true;
    private int coneHealth = CONE_HEALTH;

    // Textures & Anims
    private final Array<Texture> coneWalkTex, coneEatTex;
    private final Animation<TextureRegion> coneWalkAnim, coneEatAnim;

    private final Array<Texture> normWalkTex, normEatTex;
    private final Animation<TextureRegion> normWalkAnim, normEatAnim;

    private final Array<Texture> dieTex, headTex, charredTex;
    private final Animation<TextureRegion> dieAnim, headPopAnim, charredAnim;

    private float stateTime = 0f;
    private boolean isDying = false;
    private boolean isEating = false;
    private boolean isCharredDeath = false;
    private boolean sizeInitialized = false;

    // Lưu lại kích thước gốc để tính tỷ lệ
    private float originalW, originalH;

    public ConeheadZombie() {
        super();

        // 1. Load Animation (Dùng Linear cho mượt)
        coneWalkTex = load("images/Zombies/ConeheadZombie/Zombie/Zombie_", 63); // Code cũ 63, sửa lại 21 cho nhẹ nếu không đủ ảnh
        coneWalkAnim = anim(coneWalkTex, Animation.PlayMode.LOOP);

        coneEatTex = load("images/Zombies/ConeheadZombie/ConeheadZombieAttack/ConeheadZombieAttack_", 10);
        coneEatAnim = anim(coneEatTex, Animation.PlayMode.LOOP);

        // Load bộ Normal (khi mất nón)
        normWalkTex = load("images/Zombies/NormalZombie/Zombie/Zombie_", 21);
        normWalkAnim = anim(normWalkTex, Animation.PlayMode.LOOP);

        normEatTex = load("images/Zombies/NormalZombie/ZombieAttack/ZombieAttack_", 10);
        normEatAnim = anim(normEatTex, Animation.PlayMode.LOOP);

        // Load bộ chết
        dieTex = load("images/Zombies/NormalZombie/ZombieDie/ZombieDie_", 9);
        dieAnim = anim(dieTex, Animation.PlayMode.NORMAL);

        headTex = load("images/Zombies/NormalZombie/ZombieHead/ZombieHead_", 11);
        headPopAnim = anim(headTex, Animation.PlayMode.NORMAL);

        charredTex = load("images/Zombies/NormalZombie/BoomDie/BoomDie_", 19);
        charredAnim = anim(charredTex, Animation.PlayMode.NORMAL);

        // Lấy kích thước gốc
        TextureRegion firstFrame = coneWalkAnim.getKeyFrame(0f);
        originalW = firstFrame.getRegionWidth();
        originalH = firstFrame.getRegionHeight();
        
        this.health = BODY_HEALTH;
        this.baseSpeed = INITIAL_SPEED;
        this.speed = baseSpeed;
    }

    // Hàm thiết lập kích thước (Chỉ chạy 1 lần khi Act)
    private void initSize() {
        if (sizeInitialized) return;
        
        float worldH = (getStage() != null) ? getStage().getViewport().getWorldHeight() : ScaleManager.BASE_SCREEN_H;
        
        // Chiều cao hiển thị mong muốn (theo màn hình)
        float displayH = ScaleManager.scaleByHeight(DesignConfig.ZOMBIE_H, worldH);
        float aspect = originalW / originalH;
        float displayW = displayH * aspect;

        // [MẤU CHỐT SỬA LỖI LỆCH HÀNG]
        // Set hitbox thấp hơn thực tế để tâm (CenterY) khớp với NormalZombie
        setSize(displayW, displayH * HITBOX_SCALE); 
        
        sizeInitialized = true;
    }

    @Override
    public void act(float delta) {
        initSize();

        // Logic chết
        if (isDying) {
            stateTime += delta;
            boolean done = isCharredDeath ? charredAnim.isAnimationFinished(stateTime) : dieAnim.isAnimationFinished(stateTime);
            if (done && !dead) {
                dead = true;
                remove();
            }
            return;
        }

        super.act(delta);

        // Update time
        if (isEating) stateTime += delta;
        else {
             float scale = (speed > 0) ? (speed / baseSpeed) : 1f;
             if (scale < 0.2f) scale = 1f;
             stateTime += delta * scale;
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (dead) return;

        Color c = getColor();
        batch.setColor(c.r, c.g, c.b, c.a * parentAlpha);

        // 1. Chọn Frame
        TextureRegion frame;
        if (isDying) {
            frame = isCharredDeath ? charredAnim.getKeyFrame(stateTime) : dieAnim.getKeyFrame(stateTime);
        } else {
            if (coneOnHead) frame = isEating ? coneEatAnim.getKeyFrame(stateTime) : coneWalkAnim.getKeyFrame(stateTime);
            else            frame = isEating ? normEatAnim.getKeyFrame(stateTime) : normWalkAnim.getKeyFrame(stateTime);
        }

        // =================================================================
        // [FIX LỖI ZOMBIE BỊ TEO/BÓP MÉO]
        // =================================================================
        
        // B1: Tính chiều cao vẽ (Visual Height)
        float drawHeight;
        if (coneOnHead) {
            // Nếu có nón -> Vẽ to hơn hitbox (khôi phục 100% kích thước)
            drawHeight = getHeight() / HITBOX_SCALE; 
        } else {
            // Nếu mất nón -> Vẽ bằng hitbox (vì zombie đã lùn đi, vừa với hitbox hiện tại)
            drawHeight = getHeight();
        }

        // B2: Tính chiều rộng vẽ (Visual Width) dựa trên tỷ lệ ảnh
        // Lấy tỷ lệ của frame hiện tại (Ăn hay Đi đều sẽ có tỷ lệ riêng)
        float texAspectRatio = (float) frame.getRegionWidth() / frame.getRegionHeight();
        // Tính chiều rộng tương ứng để ảnh không bị méo
        float drawWidth = drawHeight * texAspectRatio;

        // B3: Căn giữa hình ảnh vào Hitbox
        // (HitboxWidth - DrawWidth) / 2 sẽ ra khoảng cách để căn giữa
        float drawX = getX() + (getWidth() - drawWidth) / 2f;
        
        // Vẽ từ chân (getY) lên
        batch.draw(frame, drawX, getY(), drawWidth, drawHeight);

        // Vẽ đầu rụng (nếu có)
        if (isDying && !isCharredDeath && !headPopAnim.isAnimationFinished(stateTime)) {
            TextureRegion headFrame = headPopAnim.getKeyFrame(stateTime);
            // Cũng phải tính lại tỷ lệ cho cái đầu để không bị méo
            float headRatio = (float) headFrame.getRegionWidth() / headFrame.getRegionHeight();
            float headWidth = drawHeight * headRatio;
            float headX = getX() + (getWidth() - headWidth) / 2f;

            batch.draw(headFrame, headX, getY(), headWidth, drawHeight);
        }

        batch.setColor(Color.WHITE);
    }

    @Override
    public void takeDamage(int damage) {
        if (dead || isDying) return;

        if (coneOnHead) {
            coneHealth -= damage;
            if (coneHealth <= 0) coneOnHead = false; // Vỡ nón -> Hiện đầu trọc
            return;
        }
        health -= damage;
        if (health <= 0) startDeath(false);
    }

    // --- Các hàm tiện ích ngắn gọn ---
    
    private void startDeath(boolean burnt) {
        if (isDying) return;
        isDying = true;
        isCharredDeath = burnt;
        speed = 0f;
        stateTime = 0f;
        health = 0;
        setColor(Color.WHITE);
    }

    @Override public void killByMower() { startDeath(false); }
    @Override public void killByCherryBomb() { startDeath(true); }

    @Override
    public void setEating(boolean eating) {
        if (isDying || dead || this.isEating == eating) return;
        this.isEating = eating;
        stateTime = 0f;
        this.speed = eating ? 0f : baseSpeed;
    }
    
    @Override public boolean isEating() { return isEating; }

    @Override
    public void dispose() {
        disposeArr(coneWalkTex); disposeArr(coneEatTex);
        disposeArr(normWalkTex); disposeArr(normEatTex);
        disposeArr(dieTex); disposeArr(headTex); disposeArr(charredTex);
    }

    private Array<Texture> load(String prefix, int count) {
        Array<Texture> arr = new Array<>();
        for (int i = 0; i <= count; i++) {
            try {
                Texture t = new Texture(prefix + i + ".png");
                t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
                arr.add(t);
            } catch (Exception e) {}
        }
        return arr;
    }

    private Animation<TextureRegion> anim(Array<Texture> tex, Animation.PlayMode mode) {
        TextureRegion[] frames = new TextureRegion[tex.size];
        for(int i=0; i<tex.size; i++) frames[i] = new TextureRegion(tex.get(i));
        Animation<TextureRegion> a = new Animation<>(ANIM_SPEED, frames);
        a.setPlayMode(mode);
        return a;
    }
    
    private void disposeArr(Array<Texture> arr) { if(arr!=null) for(Texture t : arr) t.dispose(); }
}