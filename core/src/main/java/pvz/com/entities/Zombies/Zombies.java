package pvz.com.entities.Zombies;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Disposable;

import pvz.com.managers.DesignConfig;

// Ensure you have these helper classes created, or remove these lines if you don't
// import pvz.com.entities.Zombies.components.ZombieSounds;
// import pvz.com.entities.Zombies.components.ZombieBounds;

public class Zombies extends Actor implements Disposable {
    // ===== STATE =====
    private boolean eating = false;

    // ===== STATIC STATE =====
    protected static int zombieCount = 0;
    protected static boolean gameOver = false; // Added back for Game Over check

    // ===== COMPOSITION =====
    protected final ZombieSounds zombieSounds = new ZombieSounds(Zombies.this);
    protected ZombieBounds zombieBounds;

    // ===== CONFIG =====
    protected float speed = 20f;
    protected int health = 100;
    
    // [SNOW PEA CONFIG]
    protected float baseSpeed = 20f;
    protected boolean isSlowed = false;
    protected float slowTimer = 0f;

    // ===== DEATH STATE =====
    protected boolean dead = false;
    private static final float NORMAL_DEATH_DURATION = 10f;
    private static final float BURN_DEATH_DURATION = 1.0f;
    private float deathTimer = 0f;
    private boolean burntDeath = false;

    public Zombies() {
        zombieCount++;

        // Default size
        setSize(DesignConfig.FIXED_WIDTH, DesignConfig.FIXED_HEIGHT);

        // Sync speed
        this.speed = 20f;
        this.baseSpeed = 20f;

        // Hitbox
        zombieBounds = new ZombieBounds(getWidth(), getHeight());
        zombieBounds.update(getX(), getY(), getWidth(), getHeight());
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        if (gameOver) return;

        // Update sounds
        zombieSounds.act(delta);

        // ----- 1. DEATH LOGIC -----
        if (dead) {
            deathTimer -= delta;
            if (deathTimer <= 0f) {
                remove();
            }
            return;
        }

        // ----- 2. SLOW LOGIC (Snow Pea) -----
        if (isSlowed) {
            slowTimer -= delta;
            if (slowTimer <= 0) {
                isSlowed = false;
                this.speed = this.baseSpeed; // Restore speed
                this.setColor(Color.WHITE);  // Restore color
            }
        }

        // ----- 3. MOVEMENT -----
        moveBy(-speed * delta, 0);
        
        // ----- 4. GAME OVER CHECK -----
        if (getX() < 0) {
            gameOver = true;
        }
        
        // Hitbox is updated in positionChanged()
    }

    @Override
    protected void positionChanged() {
        super.positionChanged();
        if (zombieBounds != null) {
            zombieBounds.update(getX(), getY(), getWidth(), getHeight());
        }
    }

    // ======================================================================
    // DEATH LOGIC
    // ======================================================================

    public void takeDamage(int dmg) {
        if (dead) return;

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
        if (dead) return;

        die(false);
        deathTimer = 0f;
        remove();
    }

    protected void die(boolean burnt) {
        if (dead) return;

        dead = true;
        burntDeath = burnt;
        speed = 0f;
        
        setColor(Color.WHITE); // Reset color on death
        setEating(false);

        if (zombieCount > 0) {
            zombieCount--;
        }

        onDie(burnt);

        deathTimer = burnt ? BURN_DEATH_DURATION : NORMAL_DEATH_DURATION;
    }

    protected void onDie(boolean burnt) {
        // subclass override
    }

    // ======================================================================
    // API
    // ======================================================================

    public boolean isDead() { return dead; }
    public boolean isBurntDeath() { return burntDeath; }

    public Rectangle getBounds() {
        return zombieBounds != null ? zombieBounds.getBounds() : null;
    }

    public boolean isEating() { return eating; }

    public void setEating(boolean eating) {
        this.eating = eating;
    }

    // ======================================================================
    // SNOW PEA API
    // ======================================================================

    public void applySlow(float duration, float factor) {
        if (dead) return;

        this.isSlowed = true;
        this.slowTimer = duration;
        this.speed = this.baseSpeed * factor;
        this.setColor(0.5f, 0.5f, 1f, 1f); // Blue tint
    }

    @Override
    public void dispose() {
        zombieSounds.dispose();
    }

    // ======================================================================
    // GETTERS
    // ======================================================================

    public static int getZombieCount() {
        return zombieCount;
    }
    
    public int getHealth() {
        return health;
    }

    public static boolean isGameOver() {
        return gameOver;
    }
}