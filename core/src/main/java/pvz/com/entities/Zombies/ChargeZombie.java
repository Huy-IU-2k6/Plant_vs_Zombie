package pvz.com.entities.Zombies;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

import pvz.com.managers.DesignConfig;
import pvz.com.managers.ScaleManager;

public class ChargeZombie extends Zombies {

    // ===== CONST =====
    private static final float BASE_SPEED = 32f;
    private static final int BASE_HEALTH = 160;

    // giống style NormalZombie
    private static final float WALK_FRAME_TIME = 0.12f;
    private static final float EAT_FRAME_TIME = 0.12f;

    // ===== TEXTURES =====
    private final Array<Texture> walkTextures;
    private final Array<Texture> eatTextures;

    // ===== ANIMS =====
    private final Animation<TextureRegion> walkAnim;
    private final Animation<TextureRegion> eatAnim;

    // ===== STATE =====
    private float stateTime = 0f;
    private boolean eating = false;

    // remove sau khi chết (giữ logic cũ)
    private float deathTimer = 0f;
    private static final float DEAD_REMOVE_DELAY = 1.2f;

    // scale
    private boolean sizeInitialized = false;
    private float originalW;
    private float originalH;

    public ChargeZombie() {
        super();

        this.baseSpeed = BASE_SPEED;
        this.speed = BASE_SPEED;
        this.health = BASE_HEALTH;

        // ===== LOAD WALK (PNG sequence) =====
        walkTextures = new Array<>();
        // Ví dụ: images/Zombies/ChargeZombie/Zombie/Zombie_0.png ...
        for (int i = 0; i <= 94; i++) {
            walkTextures.add(new Texture("images/Zombies/ChargeZombie/Zombie/Zombie_" + i + ".png"));
        }
        walkAnim = createAnimation(walkTextures, WALK_FRAME_TIME, Animation.PlayMode.LOOP);

        // ===== LOAD EAT (PNG sequence) =====
        eatTextures = new Array<>();
        // Ví dụ: images/Zombies/ChargeZombie/ZombieAttack/ZombieAttack_0.png ...
        for (int i = 0; i <= 114; i++) {
            eatTextures.add(new Texture("images/Zombies/ChargeZombie/ZombieAttack/ZombieAttack_" + i + ".png"));
        }
        eatAnim = createAnimation(eatTextures, EAT_FRAME_TIME, Animation.PlayMode.LOOP);

        // ===== INIT SIZE =====
        TextureRegion first = walkAnim.getKeyFrame(0f);
        originalW = first.getRegionWidth();
        originalH = first.getRegionHeight();
        setSize(originalW, originalH);
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

        // chết -> đếm timer rồi remove
        if (dead) {
            stateTime += delta;
            deathTimer += delta;

            if (deathTimer > DEAD_REMOVE_DELAY) {
                remove();
            }
            return;
        }

        super.act(delta);
        stateTime += delta;
    }

    @Override
    public boolean isEating() {
        return eating;
    }

    public void setEating(boolean eat) {
        if (dead)
            return;
        if (this.eating == eat)
            return;

        this.eating = eat;
        stateTime = 0f;

        if (eat) {
            this.speed = 0f;
        } else {
            this.speed = this.baseSpeed;
        }
    }

    @Override
    public void takeDamage(int dmg) {
        if (dead)
            return;

        health -= dmg;
        if (health <= 0) {
            dead = true;
            speed = 0f;
            health = 0;

            if (zombieCount > 0)
                zombieCount--;

            deathTimer = 0f;
            stateTime = 0f;
        }
    }

    @Override
    public void killByCherryBomb() {
        // giữ hành vi cũ: chết và remove sau 1.2s (không burnt anim)
        if (dead)
            return;

        dead = true;
        speed = 0f;
        health = 0;

        if (zombieCount > 0)
            zombieCount--;

        deathTimer = 0f;
        stateTime = 0f;
    }

    @Override
    public void killByMower() {
        takeDamage(999999);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (dead)
            return;

        TextureRegion frame = (eating ? eatAnim : walkAnim).getKeyFrame(stateTime);
        batch.draw(frame, getX(), getY(), getWidth(), getHeight());
    }

    public void dispose() {
        if (walkTextures != null)
            for (Texture t : walkTextures)
                t.dispose();
        if (eatTextures != null)
            for (Texture t : eatTextures)
                t.dispose();
    }
}
