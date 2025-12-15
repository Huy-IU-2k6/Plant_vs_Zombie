package pvz.com.entities.Zombies;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import pvz.com.managers.DesignConfig;
import pvz.com.managers.ScaleManager;

public class ConeheadZombie extends Zombies {

    private static final int BODY_HEALTH = 100;
    private static final int CONE_HEALTH = 200;
    private static final float INITIAL_SPEED = 18f;

    private static final float WALK_FRAME_TIME = 0.12f;
    private static final float EAT_FRAME_TIME = 0.25f;
    private static final float DIE_FRAME_TIME = 0.15f;
    private static final float HEAD_POP_FRAME_TIME = 0.1f;

    private boolean coneOnHead = true;
    private int coneHealth = CONE_HEALTH;

    private final Animation<TextureRegion> walkAnim;
    private final Animation<TextureRegion> eatAnim;
    private final Animation<TextureRegion> dieAnim;
    private final Animation<TextureRegion> headPopAnim;

    private float stateTime = 0f;
    private boolean isDying = false;
    private boolean isEating = false;

    public ConeheadZombie() {
        super();

        walkAnim = anim("images/Zombies/ConeheadZombie/Zombie/Zombie_", 62, WALK_FRAME_TIME, Animation.PlayMode.LOOP);
        eatAnim = anim("images/Zombies/ConeheadZombie/ZombieAttack/ZombieAttack_", 28, EAT_FRAME_TIME,
                Animation.PlayMode.LOOP);
        dieAnim = anim("images/Zombies/ConeheadZombie/ZombieDie/ZombieDie_", 9, DIE_FRAME_TIME,
                Animation.PlayMode.NORMAL);
        headPopAnim = anim("images/Zombies/ConeheadZombie/ZombieHead/ZombieHead_", 11, HEAD_POP_FRAME_TIME,
                Animation.PlayMode.NORMAL);

        // ===== FIX SIZE NGAY =====
        TextureRegion frame = walkAnim.getKeyFrame(0f);
        float aspect = (float) frame.getRegionWidth() / frame.getRegionHeight();
        float zombieWorldH = ScaleManager.scaleByHeight(
                DesignConfig.ZOMBIE_H,
                ScaleManager.BASE_SCREEN_H);
        setSize(zombieWorldH * aspect, zombieWorldH);

        this.health = BODY_HEALTH;
        this.baseSpeed = INITIAL_SPEED;
        this.speed = baseSpeed;
    }

    @Override
    public void act(float delta) {
        if (isDying) {
            stateTime += delta;
            if (dieAnim.isAnimationFinished(stateTime)) {
                dead = true;
                remove();
            }
            return;
        }

        super.act(delta);

        stateTime += isEating ? delta : delta * Math.max(speed / baseSpeed, 0.2f);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (dead)
            return;

        batch.setColor(getColor().r, getColor().g, getColor().b, parentAlpha);

        TextureRegion frame = isDying
                ? dieAnim.getKeyFrame(stateTime)
                : (isEating ? eatAnim : walkAnim).getKeyFrame(stateTime);

        batch.draw(frame, getX(), getY(), getWidth(), getHeight());
        batch.setColor(Color.WHITE);
    }

    @Override
    public void takeDamage(int damage) {
        if (dead || isDying)
            return;

        if (coneOnHead) {
            coneHealth -= damage;
            if (coneHealth <= 0)
                coneOnHead = false;
            return;
        }

        health -= damage;
        if (health <= 0)
            startDeath();
    }

    private void startDeath() {
        isDying = true;
        speed = 0f;
        stateTime = 0f;
    }

    private Animation<TextureRegion> anim(String prefix, int count, float time, Animation.PlayMode mode) {
        TextureRegion[] frames = new TextureRegion[count + 1];
        for (int i = 0; i <= count; i++) {
            frames[i] = new TextureRegion(new Texture(prefix + i + ".png"));
        }
        Animation<TextureRegion> a = new Animation<>(time, frames);
        a.setPlayMode(mode);
        return a;
    }
}
