package pvz.com.entities.Zombies;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.MathUtils; // Nhớ import MathUtils
import com.badlogic.gdx.scenes.scene2d.Actor;

import pvz.com.entities.Zombies.data.ZombieStats;
import pvz.com.entities.Zombies.strategy.DamageStrategy;
import pvz.com.managers.DesignConfig;
import pvz.com.managers.SoundManager;

public abstract class BaseZombie extends Actor {
    
    // --- STATIC ---
    protected static int zombieCount = 0;
    public static boolean gameOver = false;

    // --- COMPOSITION ---
    protected ZombieStats stats;
    protected DamageStrategy damageStrategy;
    
    // Biến đếm thời gian âm thanh
    private float groanTimer = 0f;
    private float chompTimer = 0f;

    // Hitbox
    protected Rectangle hitbox = new Rectangle();

    // --- ANIMATIONS ---
    protected Animation<TextureRegion> currentWalk, currentEat;
    protected Animation<TextureRegion> dieAnim, headAnim, charredAnim;

    // --- STATE ---
    protected float stateTime = 0f;
    protected boolean isEating = false;
    protected boolean isDying = false;
    protected boolean dead = false;
    protected boolean isCharred = false;

    public BaseZombie(ZombieStats stats, DamageStrategy strategy) {
        zombieCount++;
        this.stats = stats;
        this.damageStrategy = strategy;
        
        setSize(DesignConfig.FIXED_WIDTH, DesignConfig.FIXED_HEIGHT);
        
        loadAnimations();
        
        // ===============================================================
        // [FIX QUAN TRỌNG] RANDOM ĐỂ KHÔNG BỊ "ĐỒNG CA" ZOMBIE
        // Nếu không có 2 dòng này, zombie sinh ra cùng lúc sẽ kêu cùng lúc
        // ===============================================================
        this.groanTimer = MathUtils.random(0f, 3.5f);
        this.chompTimer = MathUtils.random(0f, 0.5f);
    }

    protected abstract void loadAnimations();

    @Override
    public void act(float delta) {
        super.act(delta);
        
        if (dead || gameOver) return;

        // Logic Âm thanh
        updateSounds(delta);

        // Logic Chết
        if (isDying) {
            stateTime += delta;
            Animation<TextureRegion> anim = isCharred ? charredAnim : dieAnim;
            if (anim != null && anim.isAnimationFinished(stateTime)) {
                dead = true;
                if (zombieCount > 0) zombieCount--;
                remove();
            }
            return;
        }

        // Logic Hành động
        if (isEating) {
            stateTime += delta;
        } else {
            // Di chuyển
            float moveSpeed = stats.getSpeed();
            moveBy(-moveSpeed * delta, 0);
            
            updateHitbox();

            float scale = (stats.getBaseSpeed() > 0) ? (moveSpeed / stats.getBaseSpeed()) : 1f;
            stateTime += delta * Math.max(scale, 0.2f);

            if (getX() < 0) gameOver = true;
        }
    }

    // Hàm xử lý âm thanh
    private void updateSounds(float delta) {
        if (isDying || dead) return;

        // 1. Tiếng rên (Groan)
        groanTimer += delta;
        if (groanTimer > 4.0f) {
            // 40% tỉ lệ phát tiếng
            if (MathUtils.randomBoolean(0.4f)) {
                SoundManager.playGroan();
            }
            // Reset về số âm ngẫu nhiên để lệch nhịp cho lần sau
            groanTimer = -MathUtils.random(0f, 2.0f); 
        }

        // 2. Tiếng nhai (Chomp)
        if (isEating) {
            chompTimer += delta;
            if (chompTimer > 0.6f) {
                SoundManager.playChomp();
                chompTimer = 0f;
            }
        } else {
            chompTimer = 0f;
        }
    }

    private void updateHitbox() {
        float w = getWidth() * 0.6f;
        float h = getHeight() * 0.85f;
        hitbox.set(getX() + (getWidth() - w)/2f, getY(), w, h);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (dead) return;
        Color c = getColor();
        batch.setColor(c.r, c.g, c.b, c.a * parentAlpha);

        TextureRegion frame = null;
        if (isDying) {
            frame = isCharred ? (charredAnim != null ? charredAnim.getKeyFrame(stateTime) : null) 
                             : (dieAnim != null ? dieAnim.getKeyFrame(stateTime) : null);
        } else {
            frame = isEating ? (currentEat != null ? currentEat.getKeyFrame(stateTime) : null) 
                             : (currentWalk != null ? currentWalk.getKeyFrame(stateTime) : null);
        }

        if (frame != null) {
            float ratio = (float) frame.getRegionWidth() / frame.getRegionHeight();
            float drawH = getHeight();
            if (damageStrategy.hasArmor()) drawH /= 0.85f;
            
            float drawW = drawH * ratio;
            float drawX = getX() + (getWidth() - drawW) / 2f;
            
            batch.draw(frame, drawX, getY(), drawW, drawH);
            
            if (isDying && !isCharred && headAnim != null && !headAnim.isAnimationFinished(stateTime)) {
                TextureRegion hFrame = headAnim.getKeyFrame(stateTime);
                float hRatio = (float) hFrame.getRegionWidth() / hFrame.getRegionHeight();
                float hW = drawH * hRatio;
                float hX = getX() + (getWidth() - hW) / 2f;
                batch.draw(hFrame, hX, getY(), hW, drawH);
            }
        }
        batch.setColor(Color.WHITE);
    }

    public void takeDamage(int amount) {
        if (isDying || dead) return;

        boolean statusChanged = damageStrategy.onDamage(stats, amount);
        if (statusChanged) onArmorBroken(); 

        if (stats.isDead()) startDeath(false);
    }

    protected void onArmorBroken() {}

    public void killByMower() { startDeath(false); }
    public void killByCherryBomb() { startDeath(true); }

    protected void startDeath(boolean burnt) {
        if (isDying) return;
        isDying = true;
        isCharred = burnt;
        stateTime = 0;
        stats.setSpeed(0);
        setEating(false);
        setColor(Color.WHITE);
    }

    public void setEating(boolean eating) { this.isEating = eating; }
    public boolean isEating() { return isEating; }
    public Rectangle getBounds() { return hitbox; }
    public static int getZombieCount() { return zombieCount; }
    
    public boolean isDead() { return dead; }
}