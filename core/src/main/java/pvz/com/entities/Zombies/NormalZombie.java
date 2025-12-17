package pvz.com.entities.Zombies;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

import pvz.com.managers.DesignConfig;
import pvz.com.managers.ScaleManager;

public class NormalZombie extends Zombies {

    // ===== CONST =====
    private static final int BODY_HEALTH = 100;
    private static final float INITIAL_SPEED = 15f;

    // Tốc độ Animation (Nhanh & Mượt)
    private static final float ANIM_SPEED = 0.055f;
    private static final float EAT_ANIM_SPEED = 0.08f;

    // ===== TEXTURES & ANIMATIONS =====
    private final Array<Texture> walkTex, headPopTex, bodyDieTex, eatTex, charredTex;
    private final Animation<TextureRegion> walkAnim, headPopAnim, bodyDieAnim, eatAnim, charredAnim;

    // ===== STATE =====
    private float stateTime = 0f;
    private boolean isDying = false;
    private boolean isEating = false;
    private boolean isCharredDeath = false;
    private boolean sizeInitialized = false;

    // Lưu kích thước gốc để tính tỷ lệ
    private float originalW, originalH;

    public NormalZombie() {
        super();

        // 1. Load Textures & Animations (Dùng Linear Filter)
        walkTex = load("images/Zombies/NormalZombie/Zombie/Zombie_", 21);
        walkAnim = anim(walkTex, ANIM_SPEED, Animation.PlayMode.LOOP);

        headPopTex = load("images/Zombies/NormalZombie/ZombieHead/ZombieHead_", 11);
        headPopAnim = anim(headPopTex, 0.08f, Animation.PlayMode.NORMAL);

        // Load thủ công cho bodyDie vì tên file không liên tục
        bodyDieTex = new Array<>();
        loadInto(bodyDieTex, "images/Zombies/NormalZombie/ZombieLostHead/ZombieLostHead_", 17);
        loadInto(bodyDieTex, "images/Zombies/NormalZombie/ZombieDie/ZombieDie_", 9);
        bodyDieAnim = anim(bodyDieTex, 0.08f, Animation.PlayMode.NORMAL);

        eatTex = load("images/Zombies/NormalZombie/ZombieAttack/ZombieAttack_", 10);
        eatAnim = anim(eatTex, EAT_ANIM_SPEED, Animation.PlayMode.LOOP);

        charredTex = load("images/Zombies/NormalZombie/BoomDie/BoomDie_", 19);
        charredAnim = anim(charredTex, 0.08f, Animation.PlayMode.NORMAL);

        // 2. Lấy kích thước gốc
        TextureRegion firstFrame = walkAnim.getKeyFrame(0f);
        originalW = firstFrame.getRegionWidth();
        originalH = firstFrame.getRegionHeight();

        this.health = BODY_HEALTH;
        this.baseSpeed = INITIAL_SPEED;
        this.speed = this.baseSpeed;
    }

    // Hàm thiết lập kích thước (Chỉ chạy 1 lần)
    private void initSize() {
        if (sizeInitialized)
            return;

        float worldH = (getStage() != null) ? getStage().getViewport().getWorldHeight() : ScaleManager.BASE_SCREEN_H;
        float displayH = ScaleManager.scaleByHeight(DesignConfig.ZOMBIE_H, worldH);
        float aspect = originalW / originalH;
        float displayW = displayH * aspect;

        setSize(displayW, displayH);
        sizeInitialized = true;
    }

    @Override
    public void act(float delta) {
        initSize();

        // Logic chết
        if (isDying) {
            stateTime += delta;
            boolean finished = isCharredDeath
                    ? charredAnim.isAnimationFinished(stateTime)
                    : bodyDieAnim.isAnimationFinished(stateTime);

            if (finished && !dead) {
                dead = true;
                speed = 0f;
                if (zombieCount > 0)
                    zombieCount--;
                remove();
            }
            return;
        }

        super.act(delta);

        // Update Animation Time
        if (isEating) {
            stateTime += delta;
        } else {
            float scale = (speed > 0) ? (speed / baseSpeed) : 1f;
            if (scale < 0.2f)
                scale = 1f;
            stateTime += delta * scale;
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (dead)
            return;

        Color c = getColor();
        batch.setColor(c.r, c.g, c.b, c.a * parentAlpha);

        // 1. Chọn Frame
        TextureRegion frame;
        if (isDying) {
            frame = isCharredDeath ? charredAnim.getKeyFrame(stateTime) : bodyDieAnim.getKeyFrame(stateTime);
        } else {
            frame = isEating ? eatAnim.getKeyFrame(stateTime) : walkAnim.getKeyFrame(stateTime);
        }

        // 2. [FIX MÉO HÌNH] Tự động tính chiều rộng vẽ dựa trên tỷ lệ ảnh
        float texRatio = (float) frame.getRegionWidth() / frame.getRegionHeight();
        float drawHeight = getHeight();
        float drawWidth = drawHeight * texRatio;

        // 3. Căn giữa vào Hitbox
        float drawX = getX() + (getWidth() - drawWidth) / 2f;

        batch.draw(frame, drawX, getY(), drawWidth, drawHeight);

        batch.setColor(Color.WHITE);
    }

    // ================= DAMAGE =================

    @Override
    public void takeDamage(int damage) {
        if (dead || isDying)
            return;
        health -= damage;
        if (health <= 0)
            startDeath(false);
    }

    private void startDeath(boolean burnt) {
        if (isDying)
            return;
        isDying = true;
        isCharredDeath = burnt;
        speed = 0f;
        stateTime = 0f;
        health = 0;
        setColor(Color.WHITE);
    }

    @Override
    public void killByMower() {
        startDeath(false);
    }

    @Override
    public void killByCherryBomb() {
        startDeath(true);
    }

    @Override
    public void setEating(boolean eating) {
        if (isDying || dead || this.isEating == eating)
            return;
        this.isEating = eating;
        this.speed = eating ? 0f : baseSpeed;
        // Reset time để animation ăn bắt đầu từ đầu cho mượt
        if (eating)
            stateTime = 0f;
    }

    @Override
    public boolean isEating() {
        return isEating;
    }

    // ================= UTIL & DISPOSE =================

    private Array<Texture> load(String prefix, int count) {
        Array<Texture> arr = new Array<>();
        loadInto(arr, prefix, count);
        return arr;
    }

    // Hàm load có Filter Linear (MỊN ẢNH)
    private void loadInto(Array<Texture> arr, String prefix, int count) {
        for (int i = 0; i <= count; i++) {
            try {
                Texture t = new Texture(prefix + i + ".png");
                t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
                arr.add(t);
            } catch (Exception e) {
            }
        }
    }

    private Animation<TextureRegion> anim(Array<Texture> textures, float time, Animation.PlayMode mode) {
        TextureRegion[] frames = new TextureRegion[textures.size];
        for (int i = 0; i < textures.size; i++)
            frames[i] = new TextureRegion(textures.get(i));
        Animation<TextureRegion> a = new Animation<>(time, frames);
        a.setPlayMode(mode);
        return a;
    }

    @Override
    public void dispose() {
        disposeArr(walkTex);
        disposeArr(headPopTex);
        disposeArr(bodyDieTex);
        disposeArr(eatTex);
        disposeArr(charredTex);
    }

    private void disposeArr(Array<Texture> arr) {
        if (arr != null)
            for (Texture t : arr)
                t.dispose();
    }
}
