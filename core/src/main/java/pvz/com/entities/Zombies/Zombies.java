package pvz.com.entities.Zombies;

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

    // dead = đã chết, đang trong animation chết, chuẩn bị bị remove
    protected boolean dead = false;

    // thời gian hiển thị animation chết trước khi remove
    private static final float NORMAL_DEATH_DURATION = 10f;
    private static final float BURN_DEATH_DURATION = 1.0f;
    private float deathTimer = 0f;
    private boolean burntDeath = false;

    // ===== STATE =====
    protected boolean eating = false;

    // ===== TIMERS =====
    private float soundTimer = 0f;
    private float chompTimer = 0f;

    // ===== COLLISION =====
    private final Rectangle hitBox = new Rectangle();

    // tỉ lệ hitbox so với sprite – THAY ĐỔI Ở ĐÂY NẾU MUỐN
    private static final float HITBOX_WIDTH_RATIO = 0.6f; // 60% chiều rộng
    private static final float HITBOX_HEIGHT_RATIO = 0.85f; // 85% chiều cao

    public Zombies() {
        zombieCount++;

        // âm thanh spawn
        comingZombieSound.play(0.8f);
        groanSound.play(0.6f);

        if (Math.random() < 0.3f) {
            brainzSound.play(0.7f);
        }

        // kích thước default, subclass có thể override
        setSize(70, 100);
        updateHitBox();
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
        // hitBox sẽ được update trong positionChanged()

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

    @Override
    protected void positionChanged() {
        super.positionChanged();
        updateHitBox();
    }

    /**
     * Hitbox THU NHỎ so với sprite:
     * - Hẹp hơn ở 2 bên (bỏ tay, biên thừa)
     * - Thấp hơn 1 chút (bỏ phần trên đầu nếu cần)
     */
    private void updateHitBox() {
        float fullW = getWidth();
        float fullH = getHeight();

        float w = fullW * HITBOX_WIDTH_RATIO;
        float h = fullH * HITBOX_HEIGHT_RATIO;

        // canh giữa theo chiều ngang
        float x = getX() + (fullW - w) / 2f;

        // cho hitbox bám từ chân lên (ít bị "ăn hụt" nếu đạn bay ngang thân)
        float y = getY();

        hitBox.set(x, y, w, h);
    }

    public boolean isEating() {
        return eating;
    }

    public void setEating(boolean eating) {
        this.eating = eating;
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

    public void killByCherryBomb() {
        die(true);
    }

    public void killByMower() {
        die(false);
    }

    public void instantKillByMower() {
        if (dead)
            return;

        die(false);
        deathTimer = 0f;
        remove();
    }

    protected void die(boolean burnt) {
        if (dead)
            return;

        dead = true;
        burntDeath = burnt;
        speed = 0f;

        if (zombieCount > 0) {
            zombieCount--;
        }

        onDie(burnt);

        deathTimer = burnt ? BURN_DEATH_DURATION : NORMAL_DEATH_DURATION;
    }

    protected void onDie(boolean burnt) {
        // subclass override nếu muốn
    }

    public boolean isDead() {
        return dead;
    }

    public boolean isBurntDeath() {
        return burntDeath;
    }

    public Rectangle getBounds() {
        // phòng trường hợp có ai đó thay đổi size ngoài act()
        updateHitBox();
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

    public static boolean isGameOver() {
        return gameOver;
    }

    public static int getZombieCount() {
        return zombieCount;
    }
}
