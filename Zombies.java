package pvz.com.Zombies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class Zombies extends Actor {

    // ----- STATIC STATE -----
    protected static boolean gameOver = false;
    protected static int zombieCount = 0;

    // ---- SOUNDS ----
    private static final Sound comingZombieSound = Gdx.audio.newSound(
            Gdx.files.internal("assets/sounds/zombies_are_coming.wav")
    );

    private static final Sound groanSound = Gdx.audio.newSound(
            Gdx.files.internal("assets/sounds/groan.wav")
    );

    private static final Sound brainzSound = Gdx.audio.newSound(
            Gdx.files.internal("assets/sounds/brainz.wav")
    );

    // ----- INSTANCE FIELDS -----
    protected float speed = 20f;
    private float soundTimer = 0f;

    private final Rectangle hitBox = new Rectangle();

    // ----- CONSTRUCTOR -----
    public Zombies() {
        zombieCount++;

        // Classic spawn sound
        comingZombieSound.play(0.8f);

        // Groan on spawn (optional)
        groanSound.play(0.6f);

        // Rare "Brainz..." shout on spawn
        if (Math.random() < 0.3) { // 30% chance
            brainzSound.play(0.7f);
        }

        setSize(70, 100);
    }

    // ----- ACT (update) -----
    @Override
    public void act(float delta) {
        super.act(delta);

        if (gameOver) return;

        // Move left
        moveBy(-speed * delta, 0);

        // Update hitbox
        hitBox.set(getX(), getY(), getWidth(), getHeight());

        // Handle groaning / brainz sounds over time
        soundTimer += delta;

        if (soundTimer > 4f) {  // every 4 seconds
            double r = Math.random();

            if (r < 0.7) {
                // 70% groan
                groanSound.play(0.5f);
            } else {
                // 30% brainz
                brainzSound.play(0.6f);
            }

            soundTimer = 0f;
        }

        // Check for game over
        if (getX() < 0) {
            gameOver = true;
        }
    }

    // ----- GET HITBOX -----
    public Rectangle getHitBox() {
        return hitBox;
    }

    // ----- DISPOSE -----
    public static void disposeAll() {
        comingZombieSound.dispose();
        groanSound.dispose();
        brainzSound.dispose();
    }
}
