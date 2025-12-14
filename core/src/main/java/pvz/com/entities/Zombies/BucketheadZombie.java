package pvz.com.entities.Zombies;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

import pvz.com.managers.DesignConfig;
import pvz.com.managers.ScaleManager;

public class BucketheadZombie extends Zombies {

    private static final int BODY_HEALTH = 100;
    private static final int BUCKET_HEALTH = 300;
    private static final float INITIAL_SPEED = 18f;

    private static final float WALK_FRAME_TIME = 0.12f;
    private static final float EAT_FRAME_TIME = 0.25f;
    private static final float BODY_DIE_FRAME_TIME = 0.15f;
    private static final float HEAD_POP_FRAME_TIME = 0.1f;

    private int bucketHealth = BUCKET_HEALTH;
    private boolean bucketLost = false;

    private final Array<Texture> walkTextures;
    private final Array<Texture> headPopTextures;
    private final Array<Texture> bodyDieTextures;
    private final Array<Texture> eatTextures;

    private final Animation<TextureRegion> walkAnim;
    private final Animation<TextureRegion> headPopAnim;
    private final Animation<TextureRegion> bodyDieAnim;
    private final Animation<TextureRegion> eatAnim;

    private float stateTime = 0f;
    private boolean isDying = false;
    private boolean isEating = false;

    public BucketheadZombie() {
        super();

        // WALK
        walkTextures = new Array<>();
        for (int i = 0; i <= 14; i++) {
            walkTextures.add(new Texture("images/Zombies/BucketheadZombie/Zombie/Zombie_" + i + ".png"));
        }
        walkAnim = createAnimation(walkTextures, WALK_FRAME_TIME, Animation.PlayMode.LOOP);

        // HEAD POP
        headPopTextures = new Array<>();
        for (int i = 0; i <= 10; i++) {
            headPopTextures.add(new Texture("images/Zombies/BucketheadZombie/ZombieHead/ZombieHead_" + i + ".png"));
        }
        headPopAnim = createAnimation(headPopTextures, HEAD_POP_FRAME_TIME, Animation.PlayMode.NORMAL);

        // BODY DIE (LostHead + Die)
        bodyDieTextures = new Array<>();
        for (int i = 0; i <= 17; i++) {
            bodyDieTextures
                    .add(new Texture("images/Zombies/BucketheadZombie/ZombieLostHead/ZombieLostHead_" + i + ".png"));
        }
        for (int i = 0; i <= 9; i++) {
            bodyDieTextures.add(new Texture("images/Zombies/BucketheadZombie/ZombieDie/ZombieDie_" + i + ".png"));
        }
        bodyDieAnim = createAnimation(bodyDieTextures, BODY_DIE_FRAME_TIME, Animation.PlayMode.NORMAL);

        // EAT
        eatTextures = new Array<>();
        for (int i = 0; i <= 11; i++) {
            eatTextures.add(new Texture("images/Zombies/BucketheadZombie/ZombieAttack/ZombieAttack_" + i + ".png"));
        }
        eatAnim = createAnimation(eatTextures, EAT_FRAME_TIME, Animation.PlayMode.LOOP);

        // ===== WORLD SIZE NGAY TRONG CONSTRUCTOR (CÁCH 2) =====
        TextureRegion first = walkAnim.getKeyFrame(0f);
        float aspect = (float) first.getRegionWidth() / (float) first.getRegionHeight();

        float zombieWorldH = ScaleManager.scaleByHeight(
                DesignConfig.ZOMBIE_H,
                ScaleManager.BASE_SCREEN_H);
        setSize(zombieWorldH * aspect, zombieWorldH);

        // STATS
        this.health = BODY_HEALTH;
        this.baseSpeed = INITIAL_SPEED;
        this.speed = this.baseSpeed;

        this.bucketHealth = BUCKET_HEALTH;
        this.bucketLost = false;
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

    @Override
    public void act(float delta) {
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

        if (isEating) {
            stateTime += delta;
        } else {
            float animSpeedScale = (speed > 0f) ? (speed / baseSpeed) : 1f;
            stateTime += delta * Math.max(animSpeedScale, 0.2f);
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (dead)
            return;

        Color c = getColor();
        batch.setColor(c.r, c.g, c.b, c.a * parentAlpha);

        if (isDying) {
            TextureRegion bodyFrame = bodyDieAnim.getKeyFrame(stateTime);
            batch.draw(bodyFrame, getX(), getY(), getWidth(), getHeight());

            if (!headPopAnim.isAnimationFinished(stateTime)) {
                TextureRegion headFrame = headPopAnim.getKeyFrame(stateTime);
                batch.draw(headFrame, getX(), getY(), getWidth(), getHeight());
            }
        } else {
            TextureRegion frame = (isEating ? eatAnim : walkAnim).getKeyFrame(stateTime);
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

        // bucket trước
        if (!bucketLost) {
            bucketHealth -= damage;
            if (bucketHealth <= 0) {
                bucketLost = true;
                bucketHealth = 0;
            }
            return;
        }

        // thân
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

        if (eating)
            this.speed = 0f;
        else
            this.speed = this.baseSpeed;
    }

    public boolean isBucketLost() {
        return bucketLost;
    }

    public int getBucketHealth() {
        return bucketHealth;
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
