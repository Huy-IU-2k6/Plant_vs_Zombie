package pvz.com.entities.Zombies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.graphics.Color;
public class Zombies extends Actor {

    // ===== STATIC STATE =====
    protected static boolean gameOver = false;
    protected static int zombieCount = 0;

    // ===== SOUNDS =====
    private static final Sound comingZombieSound = Gdx.audio.newSound(
            Gdx.files.internal("sounds/zombies_are_coming.wav"));
    private static final Sound groanSound = Gdx.audio.newSound(
            Gdx.files.internal("sounds/groan.wav"));
    private static final Sound brainzSound = Gdx.audio.newSound(
            Gdx.files.internal("sounds/brainz.wav"));
    private static final Sound chompSound = Gdx.audio.newSound(
            Gdx.files.internal("sounds/chomp.wav"));

    // ===== CONFIG =====
    protected float speed = 20f;
    protected int health = 100;
    protected float baseSpeed = 20f; 
    protected boolean isSlowed = false; 
    protected float slowTimer = 0f;     
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
        this.speed = baseSpeed;
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
                if (isSlowed) {
                    slowTimer -= delta;
            // Nếu hết thời gian làm chậm
            if (slowTimer <= 0) {
                isSlowed = false;
                this.speed = this.baseSpeed; // Trả lại tốc độ gốc
                this.setColor(Color.WHITE);  // Trả lại màu trắng bình thường
            }
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
    public void applySlow(float duration, float factor) {
    // 1. Nếu zombie đã chết thì không làm gì cả
    if (dead) return;

    // 2. Kích hoạt trạng thái bị làm chậm
    this.isSlowed = true;
    
    // 3. Gán thời gian (nếu đang bị chậm rồi thì reset lại thời gian mới)
    this.slowTimer = duration;

    // 4. Tính toán tốc độ mới
    // LƯU Ý: Phải nhân từ baseSpeed (tốc độ gốc), không nhân trực tiếp vào this.speed
    // để tránh trường hợp bị nhân chồng chéo (0.5 * 0.5 * ...) khiến zombie đứng yên.
    this.speed = this.baseSpeed * factor;

    // 5. Đổi màu Zombie sang xanh nhạt để người chơi nhận biết
    // (R=0.5, G=0.5, B=1.0, Alpha=1.0)
    this.setColor(0.5f, 0.5f, 1f, 1f);
    }
}
