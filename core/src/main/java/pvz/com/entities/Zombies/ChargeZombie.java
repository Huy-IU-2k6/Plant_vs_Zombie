package pvz.com.entities.Zombies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.Texture;

import pvz.com.managers.DesignConfig;
import pvz.com.managers.ScaleManager;
import pvz.com.managers.GifManager;

public class ChargeZombie extends Zombies {

    // ===== CONST =====
    private static final float BASE_ZOMBIE_H = DesignConfig.ZOMBIE_H;
    private static final float BASE_SPEED = 32f;
    private static final int BASE_HEALTH = 160;

    private static final float WALK_FRAME_TIME = 0.12f;
    private static final float EAT_FRAME_TIME = 0.12f;
    private static final int FRAMES_PER_ROW = 1;

    // ===== ATLAS & ANIM =====
    // Spritesheets
    private final Texture walkSheet;
    private final Texture eatSheet;

    private final Animation<TextureRegion> walkAnim;
    private final Animation<TextureRegion> eatAnim;

    // Kích thước frame gốc để giữ tỉ lệ
    private final float originalW;
    private final float originalH;

    // ===== STATE =====
    private float stateTime = 0f;
    private boolean eating = false;

    // Khi dying/burnt, auto-remove sau 1 thời gian
    private float deathTimer = 0f;

    // ===== SCALE CACHE =====
    private boolean sizeInitialized = false;
    private float lastWorldHeight = -1f;

    public ChargeZombie() {
        super();

        // Cấu hình cơ bản
        this.speed = BASE_SPEED;
        this.health = BASE_HEALTH;

        walkSheet = new Texture(Gdx.files.internal("images/Zombies/FlagZombie.gif"));
        eatSheet = new Texture(
                Gdx.files.internal("images/Zombies/FlagZombie_Eat.gif"));

        walkAnim = GifManager.createAnim(
                walkSheet, FRAMES_PER_ROW, WALK_FRAME_TIME, Animation.PlayMode.LOOP);
        eatAnim = GifManager.createAnim(
                eatSheet, FRAMES_PER_ROW, EAT_FRAME_TIME, Animation.PlayMode.LOOP);

        // Kích thước gốc từ frame đầu của walk
        TextureRegion first = walkAnim.getKeyFrame(0f);
        originalW = first.getRegionWidth();
        originalH = first.getRegionHeight();

        // Tạm set size theo layout gốc, sẽ scale lại khi có stage
        float aspect = originalW / originalH;
        setSize(aspect * BASE_ZOMBIE_H, BASE_ZOMBIE_H);
    }

    // ===== SCALE THEO WORLD =====
    private void updateSizeForWorld() {
        float worldHeight;
        if (getStage() != null && getStage().getViewport() != null) {
            worldHeight = getStage().getViewport().getWorldHeight();
        } else {
            worldHeight = ScaleManager.BASE_SCREEN_H; // fallback
        }

        if (sizeInitialized && lastWorldHeight == worldHeight) {
            return;
        }

        float zombieWorldH = ScaleManager.scaleByHeight(BASE_ZOMBIE_H, worldHeight);
        float aspect = originalW / originalH;
        float zombieWorldW = zombieWorldH * aspect;

        setSize(zombieWorldW, zombieWorldH);

        sizeInitialized = true;
        lastWorldHeight = worldHeight;
    }

    @Override
    public void act(float delta) {
        // scale size theo world
        updateSizeForWorld();

        // Nếu đã dead (dying hoặc burnt) thì chỉ chạy timer remove
        if (dead) {
            stateTime += delta;
            deathTimer += delta;

            // nếu muốn chính xác hơn có thể check isAnimationFinished()
            if (deathTimer > 1.2f) {
                remove();
            }
            return;
        }

        // Logic chung (move, gameOver, v.v.)
        super.act(delta);

        stateTime += delta;
    }

    @Override
    public boolean isEating() {
        return eating;
    }

    // Call từ collision / plant-contact logic
    public void setEating(boolean eat) {
        if (dead)
            return;

        if (this.eating == eat)
            return;

        this.eating = eat;
        stateTime = 0f;

        if (eat) {
            this.speed = 0f;
        } else {
            this.speed = BASE_SPEED;
        }
    }

    // -------- DAMAGE SYSTEM --------
    @Override
    public void takeDamage(int dmg) {
        if (dead)
            return;

        health -= dmg;
        if (health <= 0) {
            dead = true;
            speed = 0f;
            if (zombieCount > 0)
                zombieCount--;
            deathTimer = 0f;
            stateTime = 0f;
        }
    }

    // -------- Cherry Bomb (Instant Burn) --------
    @Override
    public void killByCherryBomb() {
        if (dead)
            return;

        dead = true;
        speed = 0f;
        health = 0;

        if (zombieCount > 0)
            zombieCount--;

        deathTimer = 0f;
        stateTime = 0f;
    }

    @Override
    public void killByMower() {
        takeDamage(9999); // insta kill
    }

    // -------- RENDER --------
    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (getStage() == null)
            return;

        TextureRegion frame;

        if (eating) {
            frame = eatAnim.getKeyFrame(stateTime);
        } else {
            frame = walkAnim.getKeyFrame(stateTime);
        }
        batch.draw(frame, getX(), getY(), getWidth(), getHeight());
    }

    public void dispose() {
    }
}
