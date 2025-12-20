package pvz.com.entities.Zombies;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import pvz.com.entities.Zombies.ZombieSounds;

import pvz.com.entities.Zombies.data.ZombieStats;
import pvz.com.entities.Zombies.strategy.DamageStrategy;
import pvz.com.managers.DesignConfig;

public abstract class BaseZombie extends Actor {
    

    protected static int zombieCount = 0;
    public static boolean gameOver = false;


    protected ZombieStats stats;
    protected DamageStrategy damageStrategy;
    

    protected Rectangle hitbox = new Rectangle();
    protected ZombieSounds sounds;


    protected Animation<TextureRegion> currentWalk, currentEat;
    protected Animation<TextureRegion> dieAnim, headAnim, charredAnim;


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
        this.sounds = new ZombieSounds(this);
    }

    protected abstract void loadAnimations();

    @Override
    public void act(float delta) {
        super.act(delta);
        if (sounds != null && !dead && !isDying) {
            sounds.act(delta, isEating);
        }
        if (dead || gameOver) return;


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


        if (isEating) {
            stateTime += delta;
        } else {

            float moveSpeed = stats.getSpeed();
            moveBy(-moveSpeed * delta, 0);
            

            updateHitbox();


            float scale = (stats.getBaseSpeed() > 0) ? (moveSpeed / stats.getBaseSpeed()) : 1f;
            stateTime += delta * Math.max(scale, 0.2f);


            if (getX() < 0) gameOver = true;
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