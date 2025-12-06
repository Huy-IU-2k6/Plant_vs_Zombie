package pvz.com.entities.Zombies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;

public class ZombieSounds {
    // ===== TIMERS =====
    private float soundTimer = 0f;
    private float chompTimer = 0f;

    // ===== SOUNDS =====
    public static final Sound comingZombieSound = Gdx.audio.newSound(
            Gdx.files.internal("sounds/zombies_are_coming.wav"));
    public static final Sound groanSound = Gdx.audio.newSound(
            Gdx.files.internal("sounds/groan.wav"));
    public static final Sound brainzSound = Gdx.audio.newSound(
            Gdx.files.internal("sounds/brainz.wav"));
    public static final Sound chompSound = Gdx.audio.newSound(
            Gdx.files.internal("sounds/chomp.wav"));

    private Zombies zombies;

    public ZombieSounds(Zombies zombies) {
        // tiếng khi mới xuất hiện
        comingZombieSound.play(0.8f);
        groanSound.play(0.6f);

        if (Math.random() < 0.3f) {
            brainzSound.play(0.7f);
        }
        this.zombies = zombies;
    }

    public void act(float delta) {
        // random tiếng rên (groan/brainz)
        soundTimer += delta;
        if (soundTimer > 4f) {
            double r = Math.random();
            if (r < 0.7)
                groanSound.play(0.5f);
            else
                brainzSound.play(0.6f);
            soundTimer = 0f;
        }

        // tiếng cắn khi đang ăn
        if (zombies.isEating()) {
            chompTimer += delta;
            if (chompTimer > 0.85f) {
                chompSound.play(0.7f);
                chompTimer = 0f;
            }
        } else {
            chompTimer = 0f;
        }
    }

    public static void disposeSounds() {
        comingZombieSound.dispose();
        groanSound.dispose();
        brainzSound.dispose();
        chompSound.dispose();
    }
}
