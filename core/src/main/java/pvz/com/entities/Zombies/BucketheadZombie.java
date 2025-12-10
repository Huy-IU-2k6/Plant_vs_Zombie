package pvz.com.entities.Zombies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import pvz.com.managers.GifManager;
import pvz.com.managers.DesignConfig;
import pvz.com.managers.ScaleManager;

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

    // Chiều cao zombie theo layout gốc 1920x1080
    private static final float BASE_ZOMBIE_H = DesignConfig.ZOMBIE_H;

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

    // Giữ kích thước frame gốc để tính tỉ lệ
    private final float bucketOriginalW;
    private final float bucketOriginalH;
    private final float normalOriginalW;
    private final float normalOriginalH;

    // ---------- SCALE CACHE ----------
    private boolean sizeInitialized = false;
    private boolean lastBucketLost = false;
    private float lastWorldHeight = -1f;

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

        // --------- LẤY KÍCH THƯỚC GỐC ----------
        TextureRegion bucketFirst = walkAnim.getKeyFrame(0f);
        bucketOriginalW = bucketFirst.getRegionWidth();
        bucketOriginalH = bucketFirst.getRegionHeight();

        TextureRegion normalFirst = normalWalkAnim.getKeyFrame(0f);
        normalOriginalW = normalFirst.getRegionWidth();
        normalOriginalH = normalFirst.getRegionHeight();

        // Tạm set size, sẽ scale đúng khi có stage
        float aspect = bucketOriginalW / bucketOriginalH;
        setSize(aspect * BASE_ZOMBIE_H, BASE_ZOMBIE_H);

        // Zombie groan sound
        groanSound = Gdx.audio.newSound(Gdx.files.internal("sounds/groan.wav"));
        groanSound.play(0.15f);
    }

    // ------------------------------------------------------
    // SCALE THEO WORLD / FORM
    // ------------------------------------------------------
    private void updateSizeForCurrentForm() {
        float worldHeight;
        if (getStage() != null && getStage().getViewport() != null) {
            worldHeight = getStage().getViewport().getWorldHeight();
        } else {
            // fallback: chưa có stage thì dùng layout gốc
            worldHeight = ScaleManager.BASE_SCREEN_H;
        }

        // Nếu không đổi form và worldHeight giữ nguyên → bỏ qua
        if (sizeInitialized && lastBucketLost == bucketLost && lastWorldHeight == worldHeight) {
            return;
        }

        float zombieWorldH = ScaleManager.scaleByHeight(BASE_ZOMBIE_H, worldHeight);

        float aspect;
        if (!bucketLost) {
            aspect = bucketOriginalW / bucketOriginalH;
        } else {
            aspect = normalOriginalW / normalOriginalH;
        }

        float zombieWorldW = zombieWorldH * aspect;

        setSize(zombieWorldW, zombieWorldH);

        sizeInitialized = true;
        lastBucketLost = bucketLost;
        lastWorldHeight = worldHeight;
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
                bucketLost = true;
                stateTime = 0f; // reset anim cho mượt
                updateSizeForCurrentForm(); // đổi sang size normal
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
        // Scale theo world + form (có/không có xô)
        updateSizeForCurrentForm();

        // Nếu đang cháy thì không còn di chuyển nữa,
        // chỉ để draw() lo animation và remove
        if (isBurnt) {
            stateTime += delta;
            return;
        }

        // Nếu đã chết theo kiểu thường (die()) thì thôi
        if (dead) {
            return;
        }

        super.act(delta); // xử lý move, sound, gameOver ở base

        stateTime += delta;
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
    public void setEating(boolean eating) {
        if (dead || isBurnt)
            return;

        if (this.isEating == eating)
            return;

        this.isEating = eating;
        stateTime = 0f;

        if (eating) {
            this.speed = 0f;
        } else {
            this.speed = MOVE_SPEED;
        }
    }

    public void startEating() {
        setEating(true);
    }

    public void stopEating() {
        setEating(false);
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
