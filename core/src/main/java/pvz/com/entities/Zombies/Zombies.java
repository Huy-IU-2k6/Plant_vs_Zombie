package pvz.com.entities.zombies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class Zombies extends Actor {

    // ===== STATIC STATE =====
    protected static boolean gameOver = false;
    protected static int zombieCount = 0;

    // ===== SOUNDS =====
    private static final Sound comingZombieSound = Gdx.audio.newSound(
            Gdx.files.internal("assets/sounds/zombies_are_coming.wav"));
    private static final Sound groanSound = Gdx.audio.newSound(
            Gdx.files.internal("assets/sounds/groan.wav"));
    private static final Sound brainzSound = Gdx.audio.newSound(
            Gdx.files.internal("assets/sounds/brainz.wav"));
    private static final Sound chompSound = Gdx.audio.newSound(
            Gdx.files.internal("assets/sounds/chomp.wav"));

    // ===== CONFIG =====
    protected float speed = 20f;
    protected int health = 100;

    // dead = đã chết, đang nằm trong animation chết, chuẩn bị bị remove
    protected boolean dead = false;

    // thời gian hiển thị animation chết trước khi remove
    private static final float NORMAL_DEATH_DURATION = 10f;
    private static final float BURN_DEATH_DURATION = 1.0f;
    private float deathTimer = 0f;
    private boolean burntDeath = false;

    // ===== TIMERS =====
    private float soundTimer = 0f;
    private float chompTimer = 0f;

    // ===== COLLISION =====
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

        if (gameOver)
            return;

        // ----- ĐANG CHẾT: chỉ đếm timer, hết thì remove -----
        if (dead) {
            deathTimer -= delta;
            if (deathTimer <= 0f) {
                remove();
            }
            return;
        }

        // ----- ZOMBIE CÒN SỐNG: di chuyển, âm thanh, cắn cây -----
        moveBy(-speed * delta, 0);

        hitBox.set(getX(), getY(), getWidth(), getHeight());

        // random tiếng rên
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
        if (isEating()) {
            chompTimer += delta;
            if (chompTimer > 0.85f) {
                chompSound.play(0.7f);
                chompTimer = 0f;
            }
        } else {
            chompTimer = 0f;
        }

        // chạm nhà -> thua
        if (getX() < 0) {
            gameOver = true;
        }
    }

    /** Subclass override nếu có trạng thái EATING riêng. */
    public boolean isEating() {
        return false;
    }

    // ======================================================================
    // DEATH LOGIC
    // ======================================================================

    public void takeDamage(int dmg) {
        if (dead)
            return;

        health -= dmg;
        if (health <= 0) {
            die(false);
        }
    }

    /** Cherry bomb: chết cháy ngay lập tức (burnt animation). */
    public void killByCherryBomb() {
        die(true);
    }

    /** Bị lawn mower cán: có thể dùng die(false) (cho phép animation thường). */
    public void killByMower() {
        die(false);
    }

    /** Nếu muốn mower giết *instant* không cần animation thì gọi hàm này. */
    public void instantKillByMower() {
        if (dead)
            return;

        die(false);
        // bỏ qua animation, remove luôn
        deathTimer = 0f;
        remove();
    }

    /**
     * Hàm chết chung.
     * burnt = true nếu chết cháy (để subclass đổi sprite/animation Burnt_Zombie).
     */
    protected void die(boolean burnt) {
        if (dead)
            return;

        dead = true;
        burntDeath = burnt;
        speed = 0f;

        if (zombieCount > 0) {
            zombieCount--;
        }

        // cho subclass override hook này để đổi animation
        onDie(burnt);

        // set thời gian hiện animation chết
        deathTimer = burnt ? BURN_DEATH_DURATION : NORMAL_DEATH_DURATION;
    }

    /** Hook cho subclass (FlagZombie, ConeheadZombie, v.v.) đổi animation chết. */
    protected void onDie(boolean burnt) {
        // mặc định không làm gì, subclass tự set sprite/animation nếu muốn
    }

    public boolean isDead() {
        return dead;
    }

    public boolean isBurntDeath() {
        return burntDeath;
    }

    public Rectangle getBounds() {
        return hitBox;
    }

    // ======================================================================
    // CLEANUP
    // ======================================================================

    public static void disposeAll() {
        comingZombieSound.dispose();
        groanSound.dispose();
        brainzSound.dispose();
        chompSound.dispose();
    }
}
