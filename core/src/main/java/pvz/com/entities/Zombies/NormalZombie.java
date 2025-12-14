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
    private static final float INITIAL_SPEED = 10f;

    // Animation Speeds
    private static final float WALK_FRAME_TIME = 0.12f;
    private static final float EAT_FRAME_TIME = 0.25f;
    private static final float BODY_DIE_FRAME_TIME = 0.15f;
    private static final float HEAD_POP_FRAME_TIME = 0.1f;

    // [NEW] Charred death
    private static final float CHARRED_FRAME_TIME = 0.15f;

    // ===== TEXTURES =====
    private final Array<Texture> walkTextures;
    private final Array<Texture> headPopTextures;
    private final Array<Texture> bodyDieTextures;
    private final Array<Texture> eatTextures;
    private final Array<Texture> charredTextures;

    // ===== ANIMATIONS =====
    private final Animation<TextureRegion> walkAnim;
    private final Animation<TextureRegion> headPopAnim;
    private final Animation<TextureRegion> bodyDieAnim;
    private final Animation<TextureRegion> eatAnim;
    private final Animation<TextureRegion> charredAnim;

    // ===== STATE =====
    private float stateTime = 0f;
    private boolean isDying = false;
    private boolean isEating = false;

    // [NEW] death type
    private boolean isCharredDeath = false;

    private boolean sizeInitialized = false;
    private float originalW;
    private float originalH;

    public NormalZombie() {
        super();

        // 1. WALK
        walkTextures = loadTextures("images/Zombies/NormalZombie/Zombie/Zombie_", 21);
        walkAnim = createAnimation(walkTextures, WALK_FRAME_TIME, Animation.PlayMode.LOOP);

        // 2. HEAD POP
        headPopTextures = loadTextures("images/Zombies/NormalZombie/ZombieHead/ZombieHead_", 11);
        headPopAnim = createAnimation(headPopTextures, HEAD_POP_FRAME_TIME, Animation.PlayMode.NORMAL);

        // 3. BODY DIE (Lost Head + Die)
        bodyDieTextures = new Array<>();
        for (int i = 0; i <= 17; i++) {
            bodyDieTextures.add(new Texture("images/Zombies/NormalZombie/ZombieLostHead/ZombieLostHead_" + i + ".png"));
        }
        for (int i = 0; i <= 9; i++) {
            bodyDieTextures.add(new Texture("images/Zombies/NormalZombie/ZombieDie/ZombieDie_" + i + ".png"));
        }
        bodyDieAnim = createAnimation(bodyDieTextures, BODY_DIE_FRAME_TIME, Animation.PlayMode.NORMAL);

        // 4. EAT
        eatTextures = new Array<>();
        for (int i = 0; i <= 10; i++) {
            eatTextures.add(new Texture("images/Zombies/NormalZombie/ZombieAttack/ZombieAttack_" + i + ".png"));
        }
        eatAnim = createAnimation(eatTextures, EAT_FRAME_TIME, Animation.PlayMode.LOOP);

        // 5. CHARRED (BoomDie)
        charredTextures = loadTextures("images/Zombies/NormalZombie/BoomDie/BoomDie_", 19);
        charredAnim = createAnimation(charredTextures, CHARRED_FRAME_TIME, Animation.PlayMode.NORMAL);

        // 6. INIT SIZE & STATS
        TextureRegion firstFrame = walkAnim.getKeyFrame(0f);
        originalW = firstFrame.getRegionWidth();
        originalH = firstFrame.getRegionHeight();

        // Initial size (will be rescaled by initSizeIfNeeded)
        setSize(originalW, originalH);

        this.health = BODY_HEALTH;
        this.baseSpeed = INITIAL_SPEED;
        this.speed = this.baseSpeed;
    }

    private Array<Texture> loadTextures(String prefix, int count) {
        Array<Texture> textures = new Array<>();
        for (int i = 0; i <= count; i++) {
            textures.add(new Texture(prefix + i + ".png"));
        }
        return textures;
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
        setSize(zombieWorldH * aspect, zombieWorldH);
        sizeInitialized = true;
    }

    @Override
    public void act(float delta) {
        initSizeIfNeeded();

        // --- DEATH LOGIC ---
        if (isDying) {
            stateTime += delta;

            boolean finished = isCharredDeath
                    ? charredAnim.isAnimationFinished(stateTime)
                    : bodyDieAnim.isAnimationFinished(stateTime);

            if (finished) {
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
            if (isCharredDeath) {
                TextureRegion frame = charredAnim.getKeyFrame(stateTime);
                batch.draw(frame, getX(), getY(), getWidth(), getHeight());
            } else {
                TextureRegion bodyFrame = bodyDieAnim.getKeyFrame(stateTime);
                batch.draw(bodyFrame, getX(), getY(), getWidth(), getHeight());

                if (!headPopAnim.isAnimationFinished(stateTime)) {
                    TextureRegion headFrame = headPopAnim.getKeyFrame(stateTime);
                    batch.draw(headFrame, getX(), getY(), getWidth(), getHeight());
                }
            }
        } else {
            Animation<TextureRegion> currentAnim = isEating ? eatAnim : walkAnim;
            TextureRegion frame = currentAnim.getKeyFrame(stateTime);
            batch.draw(frame, getX(), getY(), getWidth(), getHeight());
        }

        batch.setColor(Color.WHITE);
    }

    // Backward-compatible: chết thường
    private void startDeath() {
        startDeath(false);
    }

    // burnt=false: chết thường (rụng đầu), burnt=true: chết cháy (charred)
    private void startDeath(boolean burnt) {
        if (isDying || dead)
            return;

        isDying = true;
        isCharredDeath = burnt;
        stateTime = 0f;

        health = 0;
        speed = 0f;
        setColor(Color.WHITE);
    }

    @Override
    public void takeDamage(int damage) {
        if (isDying || dead)
            return;

        health -= damage;
        if (health <= 0)
            startDeath(false);
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
    public boolean isEating() {
        return isEating;
    }

    @Override
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

    @Override
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
        if (charredTextures != null)
            for (Texture t : charredTextures)
                t.dispose();
    }
}
