package pvz.com.entities.Zombies;

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
            Gdx.files.internal("assets/sounds/zombies_are_coming.wav"));

    private static final Sound groanSound = Gdx.audio.newSound(
            Gdx.files.internal("assets/sounds/groan.wav"));

    private static final Sound brainzSound = Gdx.audio.newSound(
            Gdx.files.internal("assets/sounds/brainz.wav"));

    private static final Sound chompSound = Gdx.audio.newSound(
            Gdx.files.internal("assets/sounds/chomp.wav"));

    // ----- INSTANCE FIELDS -----
    protected float speed = 20f;

    // để các con zombie con có thể dùng chung
    protected int health = 100;
    protected boolean dead = false;

    private float soundTimer = 0f;
    private float chompTimer = 0f; // timer để loop chomp

    private final Rectangle hitBox = new Rectangle();

    // ----- CONSTRUCTOR -----
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
        if (dead)
            return; // đã chết thì không xử lý nữa

        // ---- MOVEMENT ----
        moveBy(-speed * delta, 0);

        // Update collider
        hitBox.set(getX(), getY(), getWidth(), getHeight());

        // ---- SFX: RANDOM GROAN / BRAINZ ----
        soundTimer += delta;
        if (soundTimer > 4f) {
            double r = Math.random();
            if (r < 0.7)
                groanSound.play(0.5f);
            else
                brainzSound.play(0.6f);
            soundTimer = 0f;
        }

        // ---- SFX: CHOMP LOOP WHEN EATING ----
        if (isEating()) {
            chompTimer += delta;
            if (chompTimer > 0.85f) { // mỗi ~0.85s phát lại
                chompSound.play(0.7f);
                chompTimer = 0f;
            }
        } else {
            chompTimer = 0; // reset khi không ăn
        }

        // ---- GAME OVER ----
        if (getX() < 0)
            gameOver = true;
    }

    // ----- OVERRIDABLE: zombie con sẽ override -----
    public boolean isEating() {
        return false; // mặc định zombie không ăn
    }

    // ====== CÁC HÀM MỚI CHO COMBAT ======

    /**
     * Generic takeDamage – zombie con có thể override nếu có giáp, mũ, xô,...
     */
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

            // base: biến mất luôn
            remove();
        }
    }

    /**
     * Gọi khi bị Cherry Bomb nổ trúng.
     * Zombie con có animation cháy (BurntZombie.gif) thì override.
     */
    public void killByCherryBomb() {
        // mặc định: chết instant giống mower
        killByMower();
    }

    /**
     * API public cho LawnMower / game logic gọi.
     * Mặc định gọi instantKillByMower().
     */
    public void killByMower() {
        instantKillByMower();
    }

    // ====== CÁC HÀM MỚI CHO LAWNMOWER ======

    // 1) cho LawnMower hỏi xem con này đã chết chưa
    public boolean isDead() {
        return dead;
    }

    // 2) cho LawnMower lấy bounds để overlap()
    public Rectangle getBounds() {
        return hitBox;
    }

    // 3) gọi khi bị lawnmower cán
    public void instantKillByMower() {
        if (dead)
            return;

        dead = true;
        health = 0;
        speed = 0f; // đứng yên

        if (zombieCount > 0) {
            zombieCount--;
        }

        // animation chết cụ thể thì xử lý ở subclass
        // (NormalZombie, ConeheadZombie, BucketheadZombie...)
    }

    // ----- DISPOSE -----
    public static void disposeAll() {
        comingZombieSound.dispose();
        groanSound.dispose();
        brainzSound.dispose();
        chompSound.dispose();
    }
}
