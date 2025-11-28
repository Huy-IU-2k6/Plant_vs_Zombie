package pvz.com.Zombies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import pvz.com.managers.GifManager;

public class NormalZombie extends Zombies {

    // ===== CONST =====
    private static final int MAX_HEALTH = 100;
    private static final float MOVE_SPEED = 15f;
    private static final int FRAMES_PER_ROW = 1;
    private static final float WALK_FRAME_TIME = 0.20f;
    private static final float DIE_FRAME_TIME = 0.20f;
    private static final float EAT_FRAME_TIME = 0.25f;

    // Chiều cao zombie mong muốn trong world (vừa 1 ô cỏ ~ 100)
    private static final float DESIRED_HEIGHT = 100f;

    // Spritesheets
    private final Texture walkSheet;
    private final Texture dyingSheet;
    private final Texture eatSheet;

    // Animations
    private final Animation<TextureRegion> walkAnimation;
    private final Animation<TextureRegion> dyingAnimation;
    private final Animation<TextureRegion> eatAnimation;

    // State
    private float stateTime = 0f;
    private boolean isDying = false;
    private boolean isEating = false;

    public NormalZombie() {
        super();

        // ===== Load GIFs =====
        walkSheet = new Texture(Gdx.files.internal("assets/images/Zombies/NormalZombieRun.gif"));
        dyingSheet = new Texture(Gdx.files.internal("assets/images/Zombies/ZombieDie.gif"));
        eatSheet = new Texture(Gdx.files.internal("assets/images/Zombies/NormalZombieEat.gif"));

        walkAnimation = GifManager.createAnim(walkSheet, FRAMES_PER_ROW, WALK_FRAME_TIME, Animation.PlayMode.LOOP);
        dyingAnimation = GifManager.createAnim(dyingSheet, FRAMES_PER_ROW, DIE_FRAME_TIME, Animation.PlayMode.NORMAL);
        eatAnimation = GifManager.createAnim(eatSheet, FRAMES_PER_ROW, EAT_FRAME_TIME, Animation.PlayMode.LOOP);

        // ===== Set size với scale thay vì size gốc =====
        TextureRegion firstFrame = walkAnimation.getKeyFrame(0f);
        float originalW = firstFrame.getRegionWidth();
        float originalH = firstFrame.getRegionHeight();

        float scale = DESIRED_HEIGHT / originalH;
        float desiredWidth = originalW * scale;

        setSize(desiredWidth, DESIRED_HEIGHT);

        // ===== Health & speed =====
        this.health = MAX_HEALTH;
        this.speed = MOVE_SPEED;
    }

    @Override
    public void act(float delta) {
        // Nếu đang trong animation chết → không dùng logic Zombies.act
        if (isDying) {
            stateTime += delta;

            if (dyingAnimation.isAnimationFinished(stateTime)) {
                dead = true;
                speed = 0f;
                if (zombieCount > 0) {
                    zombieCount--;
                }
                remove();
            }
            return;
        }

        // Cập nhật trạng thái ăn
        boolean touchingPlant = isTouchingPlant();
        if (touchingPlant != isEating) {
            isEating = touchingPlant;
            stateTime = 0f;
        }

        // Logic chung: di chuyển, sound, gameOver, ...
        super.act(delta);

        // cập nhật thời gian animation
        stateTime += delta;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        Animation<TextureRegion> currentAnim;

        if (isDying) {
            currentAnim = dyingAnimation;
        } else if (isEating) {
            currentAnim = eatAnimation;
        } else {
            currentAnim = walkAnimation;
        }

        TextureRegion frame = currentAnim.getKeyFrame(stateTime);

        // Vẽ theo kích thước actor (đã scale)
        batch.draw(frame, getX(), getY(), getWidth(), getHeight());
    }

    @Override
    public void takeDamage(int damage) {
        if (isDying || dead)
            return;

        health -= damage;
        if (health <= 0) {
            isDying = true;
            stateTime = 0f;
        }
    }

    @Override
    public boolean isEating() {
        return isEating;
    }

    // Tạm thời stub, bạn thay bằng logic va chạm plant sau
    private boolean isTouchingPlant() {
        return false;
    }

    public void dispose() {
        walkSheet.dispose();
        dyingSheet.dispose();
        eatSheet.dispose();
    }
}
