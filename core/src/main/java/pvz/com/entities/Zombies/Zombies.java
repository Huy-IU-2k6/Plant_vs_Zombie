package pvz.com.entities.zombies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class Zombies extends Actor {

    protected static boolean gameOver = false;
    protected static int zombieCount = 0;

    private static final Sound comingZombieSound = Gdx.audio.newSound(
            Gdx.files.internal("assets/sounds/zombies_are_coming.wav"));

    private static final Sound groanSound = Gdx.audio.newSound(
            Gdx.files.internal("assets/sounds/groan.wav"));

    private static final Sound brainzSound = Gdx.audio.newSound(
            Gdx.files.internal("assets/sounds/brainz.wav"));

    private static final Sound chompSound = Gdx.audio.newSound(
            Gdx.files.internal("assets/sounds/chomp.wav"));

    protected float speed = 20f;
    protected int health = 100;
    protected boolean dead = false;

    private float soundTimer = 0f;
    private float chompTimer = 0f;

    private final Rectangle hitBox = new Rectangle();

    public Zombies() {
        zombieCount++;

        comingZombieSound.play(0.8f);
        groanSound.play(0.6f);

        if (Math.random() < 0.3f) {
            brainzSound.play(0.7f);
        }

        setSize(70, 100);
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        if (gameOver || dead)
            return;

        moveBy(-speed * delta, 0);

        hitBox.set(getX(), getY(), getWidth(), getHeight());

        soundTimer += delta;
        if (soundTimer > 4f) {
            double r = Math.random();
            if (r < 0.7)
                groanSound.play(0.5f);
            else
                brainzSound.play(0.6f);
            soundTimer = 0f;
        }

        if (isEating()) {
            chompTimer += delta;
            if (chompTimer > 0.85f) {
                chompSound.play(0.7f);
                chompTimer = 0f;
            }
        } else {
            chompTimer = 0;
        }

        if (getX() < 0)
            gameOver = true;
    }

    public boolean isEating() {
        return false;
    }

    public void takeDamage(int dmg) {
        if (dead)
            return;

        health -= dmg;
        if (health <= 0) {
            dead = true;
            speed = 0f;

            if (zombieCount > 0) {
                zombieCount--;
            }

            remove();
        }
    }

    public void killByCherryBomb() {
        killByMower();
    }

    public void killByMower() {
        instantKillByMower();
    }

    public boolean isDead() {
        return dead;
    }

    public Rectangle getBounds() {
        return hitBox;
    }

    public void instantKillByMower() {
        if (dead)
            return;

        dead = true;
        health = 0;
        speed = 0f;

        if (zombieCount > 0)
            zombieCount--;
    }

    public static void disposeAll() {
        comingZombieSound.dispose();
        groanSound.dispose();
        brainzSound.dispose();
        chompSound.dispose();
    }
}
