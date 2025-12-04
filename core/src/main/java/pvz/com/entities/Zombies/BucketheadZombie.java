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

    // Tuỳ spritesheet của bạn, hiện đang giả sử 4 frame trên 1 hàng
    private static final int FRAMES_PER_ROW = 4;
    private static final float WALK_FRAME_TIME = 0.15f;
    private static final float EAT_FRAME_TIME = 0.15f;
    private static final float BURNT_FRAME_TIME = 0.15f;

    // Chiều cao mong muốn (đồng bộ với NormalZombie)
    private static final float DESIRED_HEIGHT = 120f;

    // ---------- STATE ----------
    private int bucketHealth;
    private boolean bucketLost = false;

    private boolean isEating = false;
    private boolean isBurnt = false;

    private float stateTime = 0f;

    // ---------- TEXTURES ----------
    private final Texture bucketWalkSheet;
    private final Texture bucketEatSheet;

    private final Texture normalWalkSheet;
    private final Texture normalEatSheet;

    private final Texture burntSheet;

    // ---------- ANIMATIONS ----------
    private final Animation<TextureRegion> walkAnim;
    private final Animation<TextureRegion> eatAnim;

    private final Animation<TextureRegion> normalWalkAnim;
    private final Animation<TextureRegion> normalEatAnim;

    private final Animation<TextureRegion> burntAnim;

    // ---------- SOUNDS ----------
    private final Sound groanSound;

    public BucketheadZombie() {
        super();

        this.health = BODY_HEALTH;
        this.bucketHealth = BUCKET_HEALTH;
        this.speed = MOVE_SPEED;

        // --------- LOAD TEXTURES ----------
        bucketWalkSheet = new Texture(
                Gdx.files.internal("images/Zombies/BucketheadZombieRun.gif"));
        bucketEatSheet = new Texture(
                Gdx.files.internal("images/Zombies/BucketheadZombieAttack.gif"));

        normalWalkSheet = new Texture(Gdx.files.internal("images/Zombies/NormalZombieRun.gif"));
        normalEatSheet = new Texture(Gdx.files.internal("images/Zombies/NormalZombieEat.gif"));

        burntSheet = new Texture(
                Gdx.files.internal("images/Zombies/BurntZombie.gif"));

        // --------- CREATE ANIMATIONS ----------
        walkAnim = GifManager.createAnim(
                bucketWalkSheet,
                FRAMES_PER_ROW,
                WALK_FRAME_TIME,
                PlayMode.LOOP);

        eatAnim = GifManager.createAnim(
                bucketEatSheet,
                FRAMES_PER_ROW,
                EAT_FRAME_TIME,
                PlayMode.LOOP);

        normalWalkAnim = GifManager.createAnim(
                normalWalkSheet,
                FRAMES_PER_ROW,
                WALK_FRAME_TIME,
                PlayMode.LOOP);

        normalEatAnim = GifManager.createAnim(
                normalEatSheet,
                FRAMES_PER_ROW,
                EAT_FRAME_TIME,
                PlayMode.LOOP);

        burntAnim = GifManager.createAnim(
                burntSheet,
                FRAMES_PER_ROW,
                BURNT_FRAME_TIME,
                PlayMode.NORMAL // cháy 1 lần rồi thôi
        );

        // --------- SCALE THE ZOMBIE HEIGHT ----------
        TextureRegion firstFrame = walkAnim.getKeyFrame(0f);
        float originalW = firstFrame.getRegionWidth();
        float originalH = firstFrame.getRegionHeight();

        float scale = DESIRED_HEIGHT / originalH;
        float desiredWidth = originalW * scale;

        setSize(desiredWidth, DESIRED_HEIGHT);

        // Zombie groan sound
        groanSound = Gdx.audio.newSound(Gdx.files.internal("sounds/groan.wav"));
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
            bucketHealth -= dmg;

            if (bucketHealth <= 0) {
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

        stateTime = 0f;
        // không remove ngay, chờ burntAnim xong trong draw()
    }

    // ------------------------------------------------------
    // UPDATE
    // ------------------------------------------------------
    @Override
    public void act(float delta) {
        super.act(delta); // xử lý move, sound, gameOver ở base

        stateTime += delta;

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
            frame = burntAnim.getKeyFrame(stateTime, false);
            batch.draw(frame, getX(), getY(), getWidth(), getHeight());

            if (burntAnim.isAnimationFinished(stateTime)) {
                remove();
            }
            return;
        }

        if (dead)
            return;

        // Buckethead animations
        if (!bucketLost) {
            frame = (isEating ? eatAnim : walkAnim)
                    .getKeyFrame(stateTime, true);
        } else {
            // After bucket breaks → normal zombie anims
            frame = (isEating ? normalEatAnim : normalWalkAnim)
                    .getKeyFrame(stateTime, true);
        }

        batch.draw(frame, getX(), getY(), getWidth(), getHeight());
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

    public void dispose() {
        bucketWalkSheet.dispose();
        bucketEatSheet.dispose();
        normalWalkSheet.dispose();
        normalEatSheet.dispose();
        burntSheet.dispose();
        groanSound.dispose();
    }
}
