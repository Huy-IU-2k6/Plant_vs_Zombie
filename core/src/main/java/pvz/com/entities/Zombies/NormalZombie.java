package pvz.com.entities.Zombies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import pvz.com.managers.GifManager;
import pvz.com.managers.DesignConfig;
import pvz.com.managers.ScaleManager;

public class NormalZombie extends Zombies {

    // ===== CONST =====
    private static final int BODY_HEALTH = 100;
    private static final float MOVE_SPEED = 15f;
    private static final int FRAMES_PER_ROW = 1;
    private static final float WALK_FRAME_TIME = 0.20f;
    private static final float DIE_FRAME_TIME = 0.20f;
    private static final float EAT_FRAME_TIME = 0.25f;

    // Kích thước zombie theo layout gốc (1920x1080)
    // -> lấy từ DesignConfig
    private static final float BASE_ZOMBIE_W = DesignConfig.ZOMBIE_W;
    private static final float BASE_ZOMBIE_H = DesignConfig.ZOMBIE_H;

    // Spritesheets
    private final Texture walkSheet;
    private final Texture dieSheet;
    private final Texture eatSheet;

    // Animations
    private final Animation<TextureRegion> walkAnim;
    private final Animation<TextureRegion> dieAnim;
    private final Animation<TextureRegion> eatAnim;

    // State animation
    private float stateTime = 0f;
    private boolean isDying = false;
    private boolean isEating = false;

    // Dùng để giữ tỉ lệ khung hình gốc của sprite
    private final float originalW;
    private final float originalH;

    // Chỉ init size một lần khi đã có stage (để lấy worldHeight)
    private boolean sizeInitialized = false;

    public NormalZombie() {
        super();

        // ===== Load GIFs =====
        walkSheet = new Texture(Gdx.files.internal("images/Zombies/NormalZombieRun.gif"));
        dieSheet = new Texture(Gdx.files.internal("images/Zombies/ZombieDie.gif"));
        eatSheet = new Texture(Gdx.files.internal("images/Zombies/NormalZombieEat.gif"));

        walkAnim = GifManager.createAnim(
                walkSheet, FRAMES_PER_ROW, WALK_FRAME_TIME, Animation.PlayMode.LOOP);
        dieAnim = GifManager.createAnim(
                dieSheet, FRAMES_PER_ROW, DIE_FRAME_TIME, Animation.PlayMode.NORMAL);
        eatAnim = GifManager.createAnim(
                eatSheet, FRAMES_PER_ROW, EAT_FRAME_TIME, Animation.PlayMode.LOOP);

        // Lấy kích thước frame gốc để giữ tỉ lệ
        TextureRegion firstFrame = walkAnim.getKeyFrame(0f);
        originalW = firstFrame.getRegionWidth();
        originalH = firstFrame.getRegionHeight();

        // Tạm set size theo kích thước design (chưa scale theo world)
        // để tránh null trong 1 số logic nào đó nếu có
        setSize(BASE_ZOMBIE_W, BASE_ZOMBIE_H);

        // ===== Health & speed =====
        this.health = BODY_HEALTH;
        this.speed = MOVE_SPEED;
    }

    /**
     * Khởi tạo lại kích thước theo world hiện tại,
     * dùng DesignConfig + ScaleManager.
     *
     * - BASE_ZOMBIE_H: chiều cao zombie trên layout gốc 1920x1080
     * - scaleByHeight: scale theo worldHeight thực tế
     * - giữ nguyên tỉ lệ originalW/originalH của sprite
     */
    private void initSizeIfNeeded() {
        if (sizeInitialized)
            return;

        float worldHeight;
        if (getStage() != null && getStage().getViewport() != null) {
            worldHeight = getStage().getViewport().getWorldHeight();
        } else {
            // fallback: nếu chưa có stage thì coi như worldHeight = layout gốc
            worldHeight = ScaleManager.BASE_SCREEN_H;
        }

        // Chiều cao zombie trên world: scale từ thiết kế gốc
        float zombieWorldH = ScaleManager.scaleByHeight(BASE_ZOMBIE_H, worldHeight);

        // Giữ tỉ lệ khung hình của GIF
        float aspect = originalW / originalH;
        float zombieWorldW = zombieWorldH * aspect;

        setSize(zombieWorldW, zombieWorldH);
        sizeInitialized = true;
    }

    @Override
    public void act(float delta) {
        // đảm bảo size đã được scale đúng theo world
        initSizeIfNeeded();

        // Nếu đang trong animation chết → chỉ chạy anim, không gọi super.act
        if (isDying) {
            stateTime += delta;

            if (dieAnim.isAnimationFinished(stateTime)) {
                dead = true;
                speed = 0f;
                if (zombieCount > 0) {
                    zombieCount--;
                }
                remove();
            }
            return;
        }

        // CollisionSystem sẽ gọi setEating(true/false).
        super.act(delta);

        // Cập nhật thời gian animation
        stateTime += delta;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (dead)
            return;

        Animation<TextureRegion> currentAnim;

        if (isDying) {
            currentAnim = dieAnim;
        } else if (isEating) {
            currentAnim = eatAnim;
        } else {
            currentAnim = walkAnim;
        }

        TextureRegion frame = currentAnim.getKeyFrame(stateTime);
        batch.draw(frame, getX(), getY(), getWidth(), getHeight());
    }

    // ===== DEATH LOGIC =====

    private void startDeath() {
        if (isDying || dead)
            return;

        isDying = true;
        stateTime = 0f;
        health = 0;
        speed = 0f;
    }

    @Override
    public void takeDamage(int damage) {
        if (isDying || dead)
            return;

        health -= damage;
        if (health <= 0) {
            startDeath();
        }
    }

    @Override
    public void killByMower() {
        startDeath();
    }

    @Override
    public void killByCherryBomb() {
        startDeath();
    }

    @Override
    public boolean isEating() {
        return isEating;
    }

    public void setEating(boolean eating) {
        if (isDying || dead)
            return;

        if (this.isEating == eating)
            return;

        this.isEating = eating;
        stateTime = 0f; // reset anim cho mượt

        if (eating) {
            this.speed = 0f;
        } else {
            this.speed = MOVE_SPEED;
        }
    }

    public void dispose() {
        walkSheet.dispose();
        dieSheet.dispose();
        eatSheet.dispose();
    }
}
