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

    private static final int BODY_HEALTH = 100;
    private static final float INITIAL_SPEED = 15f;

    private static final float WALK_FRAME_TIME = 0.12f;
    private static final float EAT_FRAME_TIME = 0.1f;
    private static final float BODY_DIE_FRAME_TIME = 0.15f; 
    private static final float HEAD_POP_FRAME_TIME = 0.1f;  
    private static final float CHARRED_FRAME_TIME = 0.15f; // [MỚI]

    private final Array<Texture> walkTextures;
    private final Array<Texture> headPopTextures;
    private final Array<Texture> bodyDieTextures;
    private final Array<Texture> eatTextures;
    private final Array<Texture> charredTextures;

    private final Animation<TextureRegion> walkAnim;
    private final Animation<TextureRegion> headPopAnim;
    private final Animation<TextureRegion> bodyDieAnim;
    private final Animation<TextureRegion> eatAnim;
    private final Animation<TextureRegion> charredAnim;

    private float stateTime = 0f;
    private boolean isDying = false;
    private boolean isEating = false;
    private boolean isCharredDeath = false;

    public NormalZombie() {
        super();

        walkTextures = loadTextures("images/Zombies/NormalZombie/Zombie/Zombie_", 21);
        walkAnim = createAnim(walkTextures, WALK_FRAME_TIME, Animation.PlayMode.LOOP);

        headPopTextures = loadTextures("images/Zombies/NormalZombie/ZombieHead/ZombieHead_", 11);
        headPopAnim = createAnim(headPopTextures, HEAD_POP_FRAME_TIME, Animation.PlayMode.NORMAL);

        bodyDieTextures = new Array<>();
        bodyDieTextures.addAll(loadTextures("images/Zombies/NormalZombie/ZombieLostHead/ZombieLostHead_", 17));
        bodyDieTextures.addAll(loadTextures("images/Zombies/NormalZombie/ZombieDie/ZombieDie_", 9));
        bodyDieAnim = createAnim(bodyDieTextures, BODY_DIE_FRAME_TIME, Animation.PlayMode.NORMAL);

        eatTextures = loadTextures("images/Zombies/NormalZombie/ZombieAttack/ZombieAttack_", 10);
        eatAnim = createAnim(eatTextures, EAT_FRAME_TIME, Animation.PlayMode.LOOP);

        charredTextures = loadTextures("images/Zombies/NormalZombie/BoomDie/BoomDie_", 19);
        charredAnim = createAnim(charredTextures, CHARRED_FRAME_TIME, Animation.PlayMode.NORMAL);

        // ===== FIX SIZE NGAY TỪ ĐẦU (WORLD SIZE) =====
        TextureRegion frame = walkAnim.getKeyFrame(0f);
        float aspect = (float) frame.getRegionWidth() / frame.getRegionHeight();

        float zombieWorldH = ScaleManager.scaleByHeight(
                DesignConfig.ZOMBIE_H,
                ScaleManager.BASE_SCREEN_H);
        setSize(zombieWorldH * aspect, zombieWorldH);

        this.health = BODY_HEALTH;
        this.baseSpeed = INITIAL_SPEED;
        this.speed = this.baseSpeed;
    }

    private Array<Texture> loadTextures(String prefix, int count) {
        Array<Texture> textures = new Array<>();
        for (int i = 0; i <= count; i++) {
            Texture tex = new Texture(prefix + i + ".png");
            tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            textures.add(tex);
        }
        return textures;
    }

    // ================= CORE =================

    @Override
    public void act(float delta) {
        if (isDying) {
            stateTime += delta;
            boolean finished = isCharredDeath
                    ? charredAnim.isAnimationFinished(stateTime)
                    : bodyDieAnim.isAnimationFinished(stateTime);

            if (finished) {
                dead = true;
                speed = 0f;
                remove();
            }
            return;
        }

        super.act(delta);

        if (isEating)
            stateTime += delta;
        else {
            float scale = speed > 0 ? speed / baseSpeed : 1f;
            stateTime += delta * Math.max(scale, 0.2f);
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (dead)
            return;

        batch.setColor(getColor().r, getColor().g, getColor().b, parentAlpha);

        if (isDying) {
            TextureRegion frame = isCharredDeath
                    ? charredAnim.getKeyFrame(stateTime)
                    : bodyDieAnim.getKeyFrame(stateTime);
            batch.draw(frame, getX(), getY(), getWidth(), getHeight());
        } else {
            TextureRegion frame = (isEating ? eatAnim : walkAnim).getKeyFrame(stateTime);
            batch.draw(frame, getX(), getY(), getWidth(), getHeight());
        }

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

    private void startDeath(boolean charred) {
        isDying = true;
        isCharredDeath = charred;
        speed = 0f;
        stateTime = 0f;
    }

    // ================= UTIL =================

    private Array<Texture> loadTextures(String prefix, int count) {
        Array<Texture> arr = new Array<>();
        for (int i = 0; i <= count; i++)
            arr.add(new Texture(prefix + i + ".png"));
        return arr;
    }

    private Animation<TextureRegion> createAnim(Array<Texture> textures, float time, Animation.PlayMode mode) {
        TextureRegion[] frames = new TextureRegion[textures.size];
        for (int i = 0; i < textures.size; i++)
            frames[i] = new TextureRegion(textures.get(i));
        Animation<TextureRegion> a = new Animation<>(time, frames);
        a.setPlayMode(mode);
        return a;
    }
}
