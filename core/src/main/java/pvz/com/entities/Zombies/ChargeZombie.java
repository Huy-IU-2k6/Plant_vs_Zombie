package pvz.com.entities.Zombies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.Batch;

/**
 * ChargeZombie = Flag Zombie (running zombie)
 * Uses:
 *  - walking: FlagZombie.gif
 *  - eating: FlagZombie_Eat.gif
 *  - normal death: ZombieDie.gif
 *  - cherry bomb death: Burnt_Zombie.gif
 */
public class ChargeZombie extends Zombies {

    // Animations
    private Animation<TextureRegion> walkAnim;
    private Animation<TextureRegion> eatAnim;
    private Animation<TextureRegion> dieAnim;
    private Animation<TextureRegion> burntAnim;

    private float stateTime = 0f;
    private boolean eating = false;
    private boolean dying = false;
    private boolean burnt = false;

    // When dying, auto-remove after timer
    private float deathTimer = 0f;

    public ChargeZombie() {
        super();

        // Faster than normal zombie
        this.speed = 32f;
        this.health = 160;

        // Sprite size
        setSize(80, 120);

        // ⬇ Load GIFs through TextureAtlas (you should pack them or convert to frame atlas)
        TextureAtlas atlas = new TextureAtlas(Gdx.files.internal("assets/zombies/FlagZombie.atlas"));

        walkAnim  = new Animation<>(0.12f, atlas.findRegions("FlagZombie"), Animation.PlayMode.LOOP);
        eatAnim   = new Animation<>(0.12f, atlas.findRegions("FlagZombie_Eat"), Animation.PlayMode.LOOP);
        dieAnim   = new Animation<>(0.12f, atlas.findRegions("ZombieDie"), Animation.PlayMode.NORMAL);
        burntAnim = new Animation<>(0.10f, atlas.findRegions("Burnt_Zombie"), Animation.PlayMode.NORMAL);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        stateTime += delta;

        if (dead) {
            deathTimer += delta;
            if (deathTimer > 1.2f) { // remove after animation finishes
                remove();
            }
            return;
        }
    }

    @Override
    public boolean isEating() {
        return eating;
    }

    // Call this from collision / plant-contact logic
    public void setEating(boolean eat) {
        this.eating = eat;
        if (eat) {
            this.speed = 0;
        } else {
            this.speed = 32f;
        }
    }

    // -------- DAMAGE SYSTEM --------
    @Override
    public void takeDamage(int dmg) {
        if (dead || burnt)
            return;

        health -= dmg;
        if (health <= 0) {
            dead = true;
            dying = true;
            speed = 0;
            if (zombieCount > 0)
                zombieCount--;
        }
    }

    // -------- Cherry Bomb (Instant Burn) --------
    @Override
    public void killByCherryBomb() {
        if (dead)
            return;

        burnt = true;
        dead = true;
        speed = 0;
        health = 0;

        if (zombieCount > 0)
            zombieCount--;

        deathTimer = 0; // Start cleanup timer
    }

    @Override
    public void killByMower() {
        takeDamage(9999); // behaves like insta kill
        dying = true;
    }

    // -------- RENDER --------
    @Override
    public void draw(Batch batch, float parentAlpha) {

        TextureRegion frame;

        if (burnt) {
            frame = burntAnim.getKeyFrame(stateTime);
        } else if (dying) {
            frame = dieAnim.getKeyFrame(stateTime);
        } else if (eating) {
            frame = eatAnim.getKeyFrame(stateTime);
        } else {
            frame = walkAnim.getKeyFrame(stateTime);
        }

        batch.draw(frame, getX(), getY(), getWidth(), getHeight());
    }
}
