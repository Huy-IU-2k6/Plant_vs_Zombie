package pvz.com.Zombies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import com.badlogic.gdx.utils.Array;
import pvz.com.managers.GifManager; // <-- thêm import này

public class NormalZombie extends Zombies {

    // ===== CONST =====
    private static final int MAX_HEALTH = 100;
    private static final float MOVE_SPEED = 50f;
    private static final int FRAMES_PER_ROW = 4;
    private static final float WALK_FRAME_TIME = 0.20f;
    private static final float DIE_FRAME_TIME = 0.20f;
    private static final float EAT_FRAME_TIME = 0.25f;

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

        // nhớ chỉnh path đúng với assets của bạn
        walkSheet = new Texture(Gdx.files.internal("assets/images/Zombies/NormalZombieRun.gif"));
        dyingSheet = new Texture(Gdx.files.internal("assets/images/Zombies/ZombieDie.gif"));
        eatSheet = new Texture(Gdx.files.internal("assets/images/Zombies/NormalZombieEat.gif"));

        walkAnimation = GifManager.createAnim(walkSheet, FRAMES_PER_ROW, WALK_FRAME_TIME, Animation.PlayMode.LOOP);
        dyingAnimation = GifManager.createAnim(dyingSheet, FRAMES_PER_ROW, DIE_FRAME_TIME, Animation.PlayMode.NORMAL);
        eatAnimation = GifManager.createAnim(eatSheet, FRAMES_PER_ROW, EAT_FRAME_TIME, Animation.PlayMode.LOOP);

        TextureRegion firstFrame = walkAnimation.getKeyFrame(0f);
        setSize(firstFrame.getRegionWidth(), firstFrame.getRegionHeight());

        this.health = MAX_HEALTH;
        this.speed = MOVE_SPEED;
    }

    // ==== phần còn lại giữ nguyên như bạn đang có ====

    @Override
    public void act(float delta) {
        super.act(delta);
        stateTime += delta;

        if (isDying) {
            if (dyingAnimation.isAnimationFinished(stateTime)) {
                super.die();
            }
            return;
        }

        boolean touchingPlant = isTouchingPlant();
        if (touchingPlant != isEating) {
            isEating = touchingPlant;
            stateTime = 0f;
        }

        if (!isEating) {
            update(delta);
        }
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
        batch.draw(frame, getX(), getY(), getWidth(), getHeight());
    }

    @Override
    public void takeDamage(int damage) {
        if (isDying)
            return;

        health -= damage;
        if (health <= 0) {
            isDying = true;
            stateTime = 0f;
        }
    }

    public void dispose() {
        walkSheet.dispose();
        dyingSheet.dispose();
        eatSheet.dispose();
    }
}
