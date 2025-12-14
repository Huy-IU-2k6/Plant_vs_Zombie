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

    // ===== CONST =====
    private static final int BODY_HEALTH = 100;
    private static final int CONE_HEALTH = 200; // đặc trưng
    private static final float INITIAL_SPEED = 10f; // giống NormalZombie

    // Animation Speeds (giống NormalZombie)
    private static final float WALK_FRAME_TIME = 0.12f;
    private static final float EAT_FRAME_TIME = 0.25f;
    private static final float BODY_DIE_FRAME_TIME = 0.15f;
    private static final float HEAD_POP_FRAME_TIME = 0.1f;

    // ===== CONE STATE (đặc trưng) =====
    private boolean coneOnHead = true;
    private int coneHealth = CONE_HEALTH;

    // ===== TEXTURES =====
    private final Array<Texture> walkTextures;
    private final Array<Texture> headPopTextures;
    private final Array<Texture> bodyDieTextures;
    private final Array<Texture> eatTextures;

    // ===== ANIMATIONS =====
    private final Animation<TextureRegion> walkAnim;
    private final Animation<TextureRegion> headPopAnim;
    private final Animation<TextureRegion> bodyDieAnim;
    private final Animation<TextureRegion> eatAnim;

    // ===== STATE =====
    private float stateTime = 0f;
    private boolean isDying = false;
    private boolean isEating = false;

    private boolean sizeInitialized = false;
    private float originalW;
    private float originalH;

    public ConeheadZombie() {
        super();

        // 1) WALK
        walkTextures = new Array<>();
        for (int i = 0; i <= 62; i++) {
            walkTextures.add(new Texture("images/Zombies/ConeheadZombie/Zombie/Zombie_" + i + ".png"));
        }
        walkAnim = createAnimation(walkTextures, WALK_FRAME_TIME, Animation.PlayMode.LOOP);

        // 2) HEAD POP
        headPopTextures = new Array<>();
        for (int i = 0; i <= 11; i++) {
            headPopTextures.add(new Texture("images/Zombies/ConeheadZombie/ZombieHead/ZombieHead_" + i + ".png"));
        }
        headPopAnim = createAnimation(headPopTextures, HEAD_POP_FRAME_TIME, Animation.PlayMode.NORMAL);

        // 3) BODY DIE (LostHead + Die)
        bodyDieTextures = new Array<>();
        for (int i = 0; i <= 17; i++) {
            bodyDieTextures.add(
                    new Texture("images/Zombies/ConeheadZombie/ZombieLostHead/ZombieLostHead_" + i + ".png"));
        }
        for (int i = 0; i <= 9; i++) {
            bodyDieTextures.add(new Texture("images/Zombies/ConeheadZombie/ZombieDie/ZombieDie_" + i + ".png"));
        }
        bodyDieAnim = createAnimation(bodyDieTextures, BODY_DIE_FRAME_TIME, Animation.PlayMode.NORMAL);

        // 4) EAT (Attack)
        eatTextures = new Array<>();
        for (int i = 0; i <= 28; i++) {
            eatTextures.add(new Texture("images/Zombies/ConeheadZombie/ZombieAttack/ZombieAttack_" + i + ".png"));
        }
        eatAnim = createAnimation(eatTextures, EAT_FRAME_TIME, Animation.PlayMode.LOOP);

        // 5) INIT SIZE & STATS
        TextureRegion firstFrame = walkAnim.getKeyFrame(0f);
        originalW = firstFrame.getRegionWidth();
        originalH = firstFrame.getRegionHeight();
        setSize(originalW, originalH);

        this.health = BODY_HEALTH;
        this.baseSpeed = INITIAL_SPEED;
        this.speed = this.baseSpeed;

        this.coneHealth = CONE_HEALTH;
        this.coneOnHead = true;
    }

    private Animation<TextureRegion> createAnimation(Array<Texture> textures, float frameDuration,
            Animation.PlayMode mode) {
        TextureRegion[] frames = new TextureRegion[textures.size];
        for (int i = 0; i < textures.size; i++) {
            frames[i] = new TextureRegion(textures.get(i));
        }
        Animation<TextureRegion> anim = new Animation<>(frameDuration, frames);
        anim.setPlayMode(mode);
        return anim;
    }

    private void initSizeIfNeeded() {
        if (sizeInitialized)
            return;

        float worldHeight = (getStage() != null && getStage().getViewport() != null)
                ? getStage().getViewport().getWorldHeight()
                : ScaleManager.BASE_SCREEN_H;

        float zombieWorldH = ScaleManager.scaleByHeight(DesignConfig.ZOMBIE_H, worldHeight);
        float aspect = originalW / originalH;
        float zombieWorldW = zombieWorldH * aspect;

        setSize(zombieWorldW, zombieWorldH);
        sizeInitialized = true;
    }

    @Override
    public void act(float delta) {
        initSizeIfNeeded();

        // --- DEATH LOGIC ---
        if (isDying) {
            stateTime += delta;

            if (bodyDieAnim.isAnimationFinished(stateTime)) {
                if (!dead) {
                    dead = true;
                    speed = 0f;
                    if (zombieCount > 0)
                        zombieCount--;
                    remove();
                }
            }
            return;
        }

        super.act(delta);

        // --- ANIMATION UPDATE ---
        if (isEating) {
            stateTime += delta;
        } else {
            float animSpeedScale = (this.speed > 0) ? (this.speed / this.baseSpeed) : 1f;
            if (animSpeedScale < 0.2f)
                animSpeedScale = 1f;
            stateTime += delta * animSpeedScale;
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (dead)
            return;

        Color color = getColor();
        batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);

        if (isDying) {
            TextureRegion bodyFrame = bodyDieAnim.getKeyFrame(stateTime);
            batch.draw(bodyFrame, getX(), getY(), getWidth(), getHeight());

            if (!headPopAnim.isAnimationFinished(stateTime)) {
                TextureRegion headFrame = headPopAnim.getKeyFrame(stateTime);
                batch.draw(headFrame, getX(), getY(), getWidth(), getHeight());
            }
        } else {
            Animation<TextureRegion> currentAnim = isEating ? eatAnim : walkAnim;
            TextureRegion frame = currentAnim.getKeyFrame(stateTime);
            batch.draw(frame, getX(), getY(), getWidth(), getHeight());
        }

        batch.setColor(Color.WHITE);
    }

    private void startDeath() {
        if (isDying || dead)
            return;
        isDying = true;
        stateTime = 0f;
        health = 0;
        speed = 0f;
        setColor(Color.WHITE);
    }

    @Override
    public void takeDamage(int damage) {
        if (isDying || dead)
            return;

        // ===== ĐẶC TRƯNG CONEHEAD: trừ cone trước =====
        if (coneOnHead) {
            coneHealth -= damage;
            if (coneHealth <= 0) {
                coneOnHead = false;
                coneHealth = 0;
                // Nếu bạn có animation riêng cho "rụng nón" thì cắm vào đây,
                // còn theo yêu cầu: phần còn lại giữ nguyên.
            }
            return;
        }

        // Sau khi rớt nón -> trừ máu thân như NormalZombie
        health -= damage;
        if (health <= 0)
            startDeath();
    }

    @Override
    public void killByMower() {
        startDeath();
    }

    @Override
    public void killByCherryBomb() {
        startDeath();
    }

    @Override
    public boolean isEating() {
        return isEating;
    }

    public void setEating(boolean eating) {
        if (isDying || dead)
            return;
        if (this.isEating == eating)
            return;

        this.isEating = eating;
        stateTime = 0f;

        if (eating) {
            this.speed = 0f;
        } else {
            this.speed = this.baseSpeed;
        }
    }

    // Optional getters (debug/UI)
    public boolean isConeOnHead() {
        return coneOnHead;
    }

    public int getConeHealth() {
        return coneHealth;
    }

    public void dispose() {
        if (walkTextures != null)
            for (Texture t : walkTextures)
                t.dispose();
        if (headPopTextures != null)
            for (Texture t : headPopTextures)
                t.dispose();
        if (bodyDieTextures != null)
            for (Texture t : bodyDieTextures)
                t.dispose();
        if (eatTextures != null)
            for (Texture t : eatTextures)
                t.dispose();
    }
}
