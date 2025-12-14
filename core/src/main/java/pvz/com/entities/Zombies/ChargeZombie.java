package pvz.com.entities.Zombies;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

import pvz.com.managers.DesignConfig;
import pvz.com.managers.ScaleManager;

public class ChargeZombie extends Zombies {

    private static final float BASE_SPEED = 32f;
    private static final int BASE_HEALTH = 160;

    private static final float WALK_FRAME_TIME = 0.12f;
    private static final float EAT_FRAME_TIME = 0.12f;

    private static final float DEAD_REMOVE_DELAY = 1.2f;

    private final Array<Texture> walkTextures;
    private final Array<Texture> eatTextures;

    private final Animation<TextureRegion> walkAnim;
    private final Animation<TextureRegion> eatAnim;

    private float stateTime = 0f;
    private boolean eating = false;

    private float deathTimer = 0f;

    public ChargeZombie() {
        super();

        this.baseSpeed = BASE_SPEED;
        this.speed = BASE_SPEED;
        this.health = BASE_HEALTH;

        // WALK
        walkTextures = new Array<>();
        for (int i = 0; i <= 94; i++) {
            walkTextures.add(new Texture("images/Zombies/ChargeZombie/Zombie/Zombie_" + i + ".png"));
        }
        walkAnim = createAnimation(walkTextures, WALK_FRAME_TIME, Animation.PlayMode.LOOP);

        // EAT
        eatTextures = new Array<>();
        for (int i = 0; i <= 114; i++) {
            eatTextures.add(new Texture("images/Zombies/ChargeZombie/ZombieAttack/ZombieAttack_" + i + ".png"));
        }
        eatAnim = createAnimation(eatTextures, EAT_FRAME_TIME, Animation.PlayMode.LOOP);

        // ===== WORLD SIZE NGAY TRONG CONSTRUCTOR (CÁCH 2) =====
        TextureRegion first = walkAnim.getKeyFrame(0f);
        float aspect = (float) first.getRegionWidth() / (float) first.getRegionHeight();

        float zombieWorldH = ScaleManager.scaleByHeight(
                DesignConfig.ZOMBIE_H,
                ScaleManager.BASE_SCREEN_H);
        setSize(zombieWorldH * aspect, zombieWorldH);
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
        // chết -> đếm timer rồi remove (giữ logic cũ)
        if (dead) {
            stateTime += delta;
            deathTimer += delta;

            if (deathTimer > DEAD_REMOVE_DELAY) {
                remove();
            }
            return;
        }

        super.act(delta);

        // update anim: đi bộ thì scale theo speed, ăn thì chạy đều
        if (eating) {
            stateTime += delta;
        } else {
            float animSpeedScale = (speed > 0f) ? (speed / baseSpeed) : 1f;
            stateTime += delta * Math.max(animSpeedScale, 0.2f);
        }
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

        if (eat)
            this.speed = 0f;
        else
            this.speed = this.baseSpeed;
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
