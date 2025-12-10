package pvz.com.entities.Zombies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import pvz.com.managers.GifManager;
import pvz.com.managers.DesignConfig;
import pvz.com.managers.ScaleManager;

public class ConeheadZombie extends Zombies {

    private static final int BODY_HEALTH = 100;
    private static final int CONE_HEALTH = 200;
    private static final float MOVE_SPEED = 50f;

    private static final int FRAMES_PER_ROW = 1;
    private static final float WALK_FRAME_TIME = 0.20f;
    private static final float EAT_FRAME_TIME = 0.25f;
    private static final float DIE_FRAME_TIME = 0.20f;

    // Kích thước zombie theo layout gốc (1920x1080)
    private static final float BASE_ZOMBIE_H = DesignConfig.ZOMBIE_H;

    // Textures
    private final Texture walkConeSheet;
    private final Texture eatConeSheet;

    private final Texture walkNormalSheet;
    private final Texture eatNormalSheet;
    private final Texture dieNormalSheet;

    private final Texture burntZombieSheet; // Cherry Bomb death

    // Animations
    private final Animation<TextureRegion> walkConeAnim;
    private final Animation<TextureRegion> eatConeAnim;

    private final Animation<TextureRegion> walkNormalAnim;
    private final Animation<TextureRegion> eatNormalAnim;
    private final Animation<TextureRegion> dieNormalAnim;
    private final Animation<TextureRegion> burntAnim; // Cherry Bomb death

    // Lưu kích thước frame gốc để giữ tỉ lệ
    private final float coneOriginalW;
    private final float coneOriginalH;
    private final float normalOriginalW;
    private final float normalOriginalH;

    // State
    private float stateTime = 0f;
    private boolean isDying = false;
    private boolean isEating = false;
    private boolean killedByCherryBomb = false;

    private boolean coneOnHead = true;
    private int coneHealth = CONE_HEALTH;

    // Cache cho việc scale
    private boolean sizeInitialized = false;
    private boolean lastConeOnHead = true;
    private float lastWorldHeight = -1f;

    public ConeheadZombie() {
        super();

        this.health = BODY_HEALTH;
        this.speed = MOVE_SPEED;

        // Load textures
        walkConeSheet = new Texture(Gdx.files.internal("images/Zombies/ConeheadZombie.gif"));
        eatConeSheet = new Texture(Gdx.files.internal("images/Zombies/ConeheadZombie_Eat.gif"));

        walkNormalSheet = new Texture(Gdx.files.internal("images/Zombies/NormalZombieRun.gif"));
        eatNormalSheet = new Texture(Gdx.files.internal("images/Zombies/NormalZombieEat.gif"));
        dieNormalSheet = new Texture(Gdx.files.internal("images/Zombies/ZombieDie.gif"));

        burntZombieSheet = new Texture(Gdx.files.internal("images/Zombies/BurntZombie.gif"));

        // Build animations bằng GifManager
        walkConeAnim = GifManager.createAnim(
                walkConeSheet, FRAMES_PER_ROW, WALK_FRAME_TIME, Animation.PlayMode.LOOP);
        eatConeAnim = GifManager.createAnim(
                eatConeSheet, FRAMES_PER_ROW, EAT_FRAME_TIME, Animation.PlayMode.LOOP);

        walkNormalAnim = GifManager.createAnim(
                walkNormalSheet, FRAMES_PER_ROW, WALK_FRAME_TIME, Animation.PlayMode.LOOP);
        eatNormalAnim = GifManager.createAnim(
                eatNormalSheet, FRAMES_PER_ROW, EAT_FRAME_TIME, Animation.PlayMode.LOOP);
        dieNormalAnim = GifManager.createAnim(
                dieNormalSheet, FRAMES_PER_ROW, DIE_FRAME_TIME, Animation.PlayMode.NORMAL);

        burntAnim = GifManager.createAnim(
                burntZombieSheet, FRAMES_PER_ROW, DIE_FRAME_TIME, Animation.PlayMode.NORMAL);

        // Kích thước frame gốc
        TextureRegion coneFirst = walkConeAnim.getKeyFrame(0f);
        coneOriginalW = coneFirst.getRegionWidth();
        coneOriginalH = coneFirst.getRegionHeight();

        TextureRegion normalFirst = walkNormalAnim.getKeyFrame(0f);
        normalOriginalW = normalFirst.getRegionWidth();
        normalOriginalH = normalFirst.getRegionHeight();

        // Tạm size bằng kích thước design, sẽ scale thật khi có stage
        setSize(normalOriginalW, BASE_ZOMBIE_H);
    }

    // ===== SCALE THEO WORLD / FORM (CONE vs NORMAL) =====
    private void updateSizeForCurrentForm() {
        float worldHeight;
        if (getStage() != null && getStage().getViewport() != null) {
            worldHeight = getStage().getViewport().getWorldHeight();
        } else {
            // fallback: cho bằng layout gốc
            worldHeight = ScaleManager.BASE_SCREEN_H;
        }

        // Nếu không đổi gì so với lần trước thì bỏ qua
        if (sizeInitialized && lastConeOnHead == coneOnHead && lastWorldHeight == worldHeight) {
            return;
        }

        // Chiều cao zombie trên world dựa trên thiết kế + scale theo chiều cao màn
        float zombieWorldH = ScaleManager.scaleByHeight(BASE_ZOMBIE_H, worldHeight);

        float aspect;
        if (coneOnHead) {
            aspect = coneOriginalW / coneOriginalH;
        } else {
            aspect = normalOriginalW / normalOriginalH;
        }

        float zombieWorldW = zombieWorldH * aspect;

        setSize(zombieWorldW, zombieWorldH);

        sizeInitialized = true;
        lastConeOnHead = coneOnHead;
        lastWorldHeight = worldHeight;
    }

    // ====== LIFE CYCLE ======

    private void startDeath(boolean byCherryBomb) {
        if (isDying || dead)
            return;

        isDying = true;
        killedByCherryBomb = byCherryBomb;
        stateTime = 0f;
        health = 0;
        speed = 0f;
    }

    @Override
    public void act(float delta) {
        // Scale theo world mỗi frame (có cache nên rẻ)
        updateSizeForCurrentForm();

        // Đang trong animation chết → chỉ chạy anim
        if (isDying) {
            stateTime += delta;

            Animation<TextureRegion> currentDieAnim = killedByCherryBomb ? burntAnim : dieNormalAnim;

            if (currentDieAnim.isAnimationFinished(stateTime)) {
                dead = true;
                speed = 0f;
                if (zombieCount > 0) {
                    zombieCount--;
                }
                remove();
            }
            return;
        }

        // Logic chung: move, sound, gameOver...
        super.act(delta);

        stateTime += delta;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (dead)
            return;

        Animation<TextureRegion> anim;

        if (isDying) {
            anim = killedByCherryBomb ? burntAnim : dieNormalAnim;
        } else if (isEating) {
            anim = coneOnHead ? eatConeAnim : eatNormalAnim;
        } else {
            anim = coneOnHead ? walkConeAnim : walkNormalAnim;
        }

        TextureRegion frame = anim.getKeyFrame(stateTime);
        batch.draw(frame, getX(), getY(), getWidth(), getHeight());
    }

    // ===== DAMAGE / DEATH =====

    @Override
    public void takeDamage(int dmg) {
        if (isDying || dead)
            return;

        if (coneOnHead) {
            coneHealth -= dmg;

            if (coneHealth <= 0) {
                coneOnHead = false;
                stateTime = 0f; // reset anim
                updateSizeForCurrentForm(); // đổi size theo sprite thường
            }
            return;
        }

        // Đã rớt nón -> máu thân
        health -= dmg;
        if (health <= 0) {
            startDeath(false);
        }
    }

    @Override
    public void killByCherryBomb() {
        startDeath(true);
    }

    @Override
    public boolean isEating() {
        return isEating;
    }

    // Hệ collision / logic bên ngoài sẽ set ăn/không ăn
    public void setEating(boolean eating) {
        if (isDying || dead)
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

    public void dispose() {
        walkConeSheet.dispose();
        eatConeSheet.dispose();

        walkNormalSheet.dispose();
        eatNormalSheet.dispose();
        dieNormalSheet.dispose();

        burntZombieSheet.dispose();
    }
}
