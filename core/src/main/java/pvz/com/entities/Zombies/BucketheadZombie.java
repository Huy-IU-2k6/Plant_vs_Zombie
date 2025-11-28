package pvz.com.entities.Zombies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import pvz.com.managers.GifManager;

public class BucketheadZombie extends Zombies {

    // ---------- CONSTANTS ----------
    private static final int BODY_HEALTH = 100;
    private static final int BUCKET_HEALTH = 300; // Bucket HP
    private static final float MOVE_SPEED = 45f;

    // tuỳ spritesheet của bạn, hiện đang giả sử 4 frame trên 1 hàng
    private static final int FRAME_COUNT = 4;
    private static final float WALK_FRAME_TIME = 0.15f;
    private static final float ATTACK_FRAME_TIME = 0.15f;
    private static final float BURNT_FRAME_TIME = 0.15f;

    // ---------- STATE ----------
    private int bucketHP;
    private boolean bucketLost = false;

    private boolean isEating = false;
    private boolean isBurnt = false;

    private float animationTimer = 0f;

    // ---------- TEXTURES ----------
    private final Texture bucketWalkSheet;
    private final Texture bucketAttackSheet;

    private final Texture normalWalkSheet;
    private final Texture normalAttackSheet;

    private final Texture burntSheet;

    // ---------- ANIMATIONS ----------
    private final Animation<TextureRegion> walkAnim;
    private final Animation<TextureRegion> attackAnim;

    private final Animation<TextureRegion> normalWalkAnim;
    private final Animation<TextureRegion> normalAttackAnim;

    private final Animation<TextureRegion> burntAnim;

    // ---------- SOUNDS ----------
    private final Sound groanSound;

    public BucketheadZombie(float x, float y) {
        setPosition(x, y);

        this.health = BODY_HEALTH;
        this.bucketHP = BUCKET_HEALTH;
        this.speed = MOVE_SPEED;

        // --------- LOAD TEXTURES ----------
        // Chỉnh path cho đúng với project của bạn
        bucketWalkSheet = new Texture(Gdx.files.internal("BucketheadZombieRun.gif"));
        bucketAttackSheet = new Texture(Gdx.files.internal("BucketheadZombieAttack.gif"));

        normalWalkSheet = new Texture(Gdx.files.internal("NormalZombieRun.gif"));
        normalAttackSheet = new Texture(Gdx.files.internal("NormalZombieAttack.gif"));

        burntSheet = new Texture(Gdx.files.internal("BurntZombie.gif"));

        // --------- CREATE ANIMATIONS ----------
        walkAnim = GifManager.createAnim(
                bucketWalkSheet,
                FRAME_COUNT,
                WALK_FRAME_TIME,
                PlayMode.LOOP);

        attackAnim = GifManager.createAnim(
                bucketAttackSheet,
                FRAME_COUNT,
                ATTACK_FRAME_TIME,
                PlayMode.LOOP);

        normalWalkAnim = GifManager.createAnim(
                normalWalkSheet,
                FRAME_COUNT,
                WALK_FRAME_TIME,
                PlayMode.LOOP);

        normalAttackAnim = GifManager.createAnim(
                normalAttackSheet,
                FRAME_COUNT,
                ATTACK_FRAME_TIME,
                PlayMode.LOOP);

        burntAnim = GifManager.createAnim(
                burntSheet,
                FRAME_COUNT,
                BURNT_FRAME_TIME,
                PlayMode.NORMAL // cháy 1 lần rồi thôi
        );

        // Zombie groan sound
        groanSound = Gdx.audio.newSound(Gdx.files.internal("assets/sounds/groan.wav"));
        groanSound.play(0.15f);
    }

    // ------------------------------------------------------
    // DAMAGE & DEATH HANDLING
    // ------------------------------------------------------
    @Override
    public void takeDamage(int dmg) {
        if (dead || isBurnt)
            return;

        // Damage bucket trước
        if (!bucketLost) {
            bucketHP -= dmg;

            if (bucketHP <= 0) {
                bucketLost = true; // không cần sound
            }
            return;
        }

        // Sau khi rớt xô thì trừ vào máu thân
        health -= dmg;

        if (health <= 0) {
            die();
        }
    }

    private void die() {
        if (dead)
            return;

        dead = true;
        speed = 0f;

        if (zombieCount > 0) {
            zombieCount--;
        }

        // Buckethead không có animation chết riêng → remove luôn
        remove();
    }

    @Override
    public void killByCherryBomb() {
        if (dead)
            return;

        // dùng BurntZombie.gif
        isBurnt = true;
        dead = true;
        speed = 0f;

        if (zombieCount > 0) {
            zombieCount--;
        }

        animationTimer = 0f;
        // không remove ngay, chờ burntAnim xong trong draw()
    }

    // ------------------------------------------------------
    // UPDATE
    // ------------------------------------------------------
    @Override
    public void act(float delta) {
        super.act(delta); // xử lý move, sound, gameOver ở base

        animationTimer += delta;

        // Khi cháy hoặc dead thì không cần xử lý thêm
        if (isBurnt || dead)
            return;
    }

    // ------------------------------------------------------
    // DRAW
    // ------------------------------------------------------
    @Override
    public void draw(Batch batch, float parentAlpha) {
        TextureRegion frame;

        // Burnt zombie death animation
        if (isBurnt) {
            frame = burntAnim.getKeyFrame(animationTimer, false);
            batch.draw(frame, getX(), getY());

            if (burntAnim.isAnimationFinished(animationTimer)) {
                remove();
            }
            return;
        }

        if (dead)
            return;

        // Buckethead animations
        if (!bucketLost) {
            frame = (isEating ? attackAnim : walkAnim)
                    .getKeyFrame(animationTimer, true);
        } else {
            // After bucket breaks → normal zombie anims
            frame = (isEating ? normalAttackAnim : normalWalkAnim)
                    .getKeyFrame(animationTimer, true);
        }

        batch.draw(frame, getX(), getY());
    }

    // ------------------------------------------------------
    // PLANT INTERACTION
    // ------------------------------------------------------
    public void startEating() {
        isEating = true;
    }

    public void stopEating() {
        isEating = false;
    }

    @Override
    public boolean isEating() {
        return isEating;
    }
}
