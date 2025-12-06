package pvz.com.entities.Zombies;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.graphics.Color;
public class Zombies extends Actor {
    // ===== STATE =====
    private boolean eating = false;

    // ===== STATIC STATE =====
    protected static boolean gameOver = false;
    protected static int zombieCount = 0;

    // ===== COMPOSITION =====
    protected final ZombieSounds zombieSounds = new ZombieSounds(Zombies.this);
    protected ZombieBounds zombieBounds;

    // ===== CONFIG =====
    protected float speed = 20f;
    protected int health = 100;
    protected float baseSpeed = 20f; 
    protected boolean isSlowed = false; 
    protected float slowTimer = 0f;     
    // dead = đã chết, đang nằm trong animation chết, chuẩn bị bị remove

    // dead = đã chết, đang trong animation chết, chuẩn bị bị remove
    protected boolean dead = false;

    // thời gian hiển thị animation chết trước khi remove
    private static final float NORMAL_DEATH_DURATION = 10f;
    private static final float BURN_DEATH_DURATION = 1.0f;
    private float deathTimer = 0f;
    private boolean burntDeath = false;

    public Zombies() {
        zombieCount++;

        // kích thước default, subclass có thể override
        setSize(70, 100);

        // tạo hitbox dựa theo size ban đầu
        zombieBounds = new ZombieBounds(getWidth(), getHeight());
        // sync lần đầu
        zombieBounds.update(getX(), getY(), getWidth(), getHeight());
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        if (gameOver)
            return;

        // update âm thanh
        zombieSounds.act(delta);

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


        // ----- ZOMBIE CÒN SỐNG: di chuyển -----
        moveBy(-speed * delta, 0);
        // hitBox sẽ được update trong positionChanged()

        // chạm nhà -> thua
        if (getX() < 0) {
            gameOver = true;
        }
        
    }

    @Override
    protected void positionChanged() {
        super.positionChanged();
        // mỗi khi Actor đổi vị trí -> cập nhật hitbox
        if (zombieBounds != null) {
            zombieBounds.update(getX(), getY(), getWidth(), getHeight());
        }
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

        // dừng trạng thái đang ăn (nếu có) để tắt tiếng chomp
        setEating(false);

        if (zombieCount > 0) {
            zombieCount--;
        }

        onDie(burnt);

        deathTimer = burnt ? BURN_DEATH_DURATION : NORMAL_DEATH_DURATION;
    }

    protected void onDie(boolean burnt) {
        // subclass override nếu muốn (đổi animation chết, v.v.)
    }

    public boolean isDead() {
        return dead;
    }

    public boolean isBurntDeath() {
        return burntDeath;
    }

    // ======================================================================
    // COLLISION API
    // ======================================================================

    public Rectangle getBounds() {
        return zombieBounds != null ? zombieBounds.getBounds() : null;
    }

    public boolean isEating() {
        return eating;
    }

    public void setEating(boolean eating) {
        this.eating = eating;
    }

    // ======================================================================
    // CLEANUP
    // ======================================================================

    public static boolean isGameOver() {
        return gameOver;
    }

    public static int getZombieCount() {
        return zombieCount;
    }
}
