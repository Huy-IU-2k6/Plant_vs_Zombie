package pvz.com.Zombies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import pvz.com.managers.GifManager;

public class BucketheadZombie extends Zombies {

    // ---------- CONSTANTS ----------
    private static final int BODY_HEALTH   = 100;
    private static final int BUCKET_HEALTH = 300;     // Bucket HP
    private static final float MOVE_SPEED  = 45f;

    // ---------- STATE ----------
    private int bucketHP;
    private boolean bucketLost = false;

    private boolean isEating = false;
    private boolean isDead = false;
    private boolean isBurnt = false;

    private float animationTimer = 0f;

    // ---------- ANIMATIONS ----------
    private Animation<TextureRegion> walkAnim;
    private Animation<TextureRegion> attackAnim;

    private Animation<TextureRegion> normalWalkAnim;
    private Animation<TextureRegion> normalAttackAnim;

    private Animation<TextureRegion> burntAnim;

    // ---------- SOUNDS ----------
    private Sound groanSound;

    public BucketheadZombie(float x, float y) {
        setPosition(x, y);

        this.health = BODY_HEALTH;
        this.bucketHP = BUCKET_HEALTH;
        this.speed = MOVE_SPEED;

        // Load animations for buckethead
        walkAnim        = GifManager.loadGifAnimation("BucketheadZombieRun.gif");
        attackAnim      = GifManager.loadGifAnimation("BucketheadZombieAttack.gif");

        // Animations after bucket falls off → NormalZombie
        normalWalkAnim   = GifManager.loadGifAnimation("NormalZombieRun.gif");
        normalAttackAnim = GifManager.loadGifAnimation("NormalZombieAttack.gif");

        // Burnt Zombie animation
        burntAnim       = GifManager.loadGifAnimation("BurntZombie.gif");

        // Zombie groan sound (if you use it globally)
        groanSound = Gdx.audio.newSound(Gdx.files.internal("groan.wav"));
        groanSound.play(0.15f);
    }

    // ------------------------------------------------------
    //                 DAMAGE & DEATH HANDLING
    // ------------------------------------------------------
    @Override
    public void takeDamage(int dmg) {
        if (isDead || isBurnt) return;

        // Damage bucket first
        if (!bucketLost) {
            bucketHP -= dmg;

            if (bucketHP <= 0) {
                bucketLost = true;  // No sound needed
            }
            return;
        }

        // Now damage zombie body
        health -= dmg;

        if (health <= 0) {
            die();
        }
    }

    private void die() {
        isDead = true;
        remove();
    }

    @Override
    public void killByCherryBomb() {
        isBurnt = true;
        isDead = true;
        animationTimer = 0;
    }

    // ------------------------------------------------------
    //                      UPDATE
    // ------------------------------------------------------
    @Override
    public void act(float delta) {
        super.act(delta);

        animationTimer += delta;

        if (isDead) return;

        if (!isEating) {
            moveBy(-speed * delta, 0);
        }
    }

    // ------------------------------------------------------
    //                      DRAW
    // ------------------------------------------------------
    @Override
    public void draw(Batch batch, float parentAlpha) {
        TextureRegion frame;

        // Burnt zombie death animation
        if (isBurnt) {
            frame = burntAnim.getKeyFrame(animationTimer, false);
            batch.draw(frame, getX(), getY());

            if (burntAnim.isAnimationFinished(animationTimer))
                remove();

            return;
        }

        if (isDead) return;

        // Buckethead animations
        if (!bucketLost) {
            frame = (isEating ? attackAnim : walkAnim)
                    .getKeyFrame(animationTimer, true);
        } 
        // After bucket breaks → normal zombie anims
        else {
            frame = (isEating ? normalAttackAnim : normalWalkAnim)
                    .getKeyFrame(animationTimer, true);
        }

        batch.draw(frame, getX(), getY());
    }

    // ------------------------------------------------------
    //              PLANT INTERACTION
    // ------------------------------------------------------
    public void startEating() { isEating = true; }
    public void stopEating()  { isEating = false; }
}
