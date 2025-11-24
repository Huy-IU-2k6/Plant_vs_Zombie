package pvz.com.zombies;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

public class ConeheadZombie extends Zombies {

    private static final int BODY_HEALTH  = 100;
    private static final int CONE_HEALTH  = 200;
    private static final float MOVE_SPEED = 50f;

    private static final int FRAMES_PER_ROW = 4;
    private static final float WALK_FRAME_TIME = 0.20f;
    private static final float EAT_FRAME_TIME  = 0.25f;
    private static final float DIE_FRAME_TIME  = 0.20f;

    // Textures
    private final Texture walkConeSheet;
    private final Texture eatConeSheet;

    private final Texture walkNormalSheet;
    private final Texture eatNormalSheet;
    private final Texture dieNormalSheet;

    // Animations
    private Animation<TextureRegion> walkConeAnim;
    private Animation<TextureRegion> eatConeAnim;

    private Animation<TextureRegion> walkNormalAnim;
    private Animation<TextureRegion> eatNormalAnim;
    private Animation<TextureRegion> dieNormalAnim; // ONLY THIS FOR DYING

    // State
    private float stateTime = 0f;
    private boolean isDying = false;
    private boolean isEating = false;

    private boolean coneOnHead = true;
    private int coneHealth = CONE_HEALTH;

    public ConeheadZombie() {
        super();

        this.health = BODY_HEALTH;
        this.speed  = MOVE_SPEED;

        // Load textures you said exist
        walkConeSheet = new Texture("ConeheadZombie.gif");
        eatConeSheet  = new Texture("ConeheadZombie_Eat.gif");

        // Normal zombie textures
        walkNormalSheet = new Texture("NormalZombieEat.gif");
        eatNormalSheet  = new Texture("NormalZombieRun.gif");
        dieNormalSheet  = new Texture("ZombieDie.gif");

        // Build animations
        walkConeAnim   = createAnim(walkConeSheet,   FRAMES_PER_ROW, WALK_FRAME_TIME, Animation.PlayMode.LOOP);
        eatConeAnim    = createAnim(eatConeSheet,    FRAMES_PER_ROW, EAT_FRAME_TIME,  Animation.PlayMode.LOOP);

        walkNormalAnim = createAnim(walkNormalSheet, FRAMES_PER_ROW, WALK_FRAME_TIME, Animation.PlayMode.LOOP);
        eatNormalAnim  = createAnim(eatNormalSheet,  FRAMES_PER_ROW, EAT_FRAME_TIME,  Animation.PlayMode.LOOP);
        dieNormalAnim  = createAnim(dieNormalSheet,  FRAMES_PER_ROW, DIE_FRAME_TIME,  Animation.PlayMode.NORMAL);

        // Set actor size to first cone frame
        TextureRegion first = walkConeAnim.getKeyFrame(0f);
        setSize(first.getRegionWidth(), first.getRegionHeight());
    }

    private Animation<TextureRegion> createAnim(Texture sheet,
                                                int frameCount,
                                                float frameDuration,
                                                Animation.PlayMode playMode) {

        int frameWidth  = sheet.getWidth() / frameCount;
        int frameHeight = sheet.getHeight();

        TextureRegion[][] tmp = TextureRegion.split(sheet, frameWidth, frameHeight);

        Array<TextureRegion> frames = new Array<>(frameCount);
        for (int i = 0; i < frameCount; i++) {
            frames.add(tmp[0][i]);
        }

        Animation<TextureRegion> anim = new Animation<>(frameDuration, frames);
        anim.setPlayMode(playMode);
        return anim;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        stateTime += delta;

        // Handle death animation fully
        if (isDying) {
            if (dieNormalAnim.isAnimationFinished(stateTime)) {
                super.die();
            }
            return;
        }

        // Eating?
        boolean touching = isTouchingPlant();
        if (touching != isEating) {
            isEating = touching;
            stateTime = 0f;
        }

        if (!isEating) {
            update(delta);
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        Animation<TextureRegion> anim;

        if (isDying) {
            anim = dieNormalAnim; // ALWAYS NORMAL ZOMBIE DIE
        } else if (isEating) {
            anim = coneOnHead ? eatConeAnim : eatNormalAnim;
        } else {
            anim = coneOnHead ? walkConeAnim : walkNormalAnim;
        }

        TextureRegion frame = anim.getKeyFrame(stateTime);
        batch.draw(frame, getX(), getY(), getWidth(), getHeight());
    }

    @Override
    public void takeDamage(int dmg) {
        if (isDying) return;

        if (coneOnHead) {
            coneHealth -= dmg;

            if (coneHealth <= 0) {
                coneOnHead = false;
                stateTime = 0f;

                // Switching to normal zombie size
                TextureRegion first = walkNormalAnim.getKeyFrame(0f);
                setSize(first.getRegionWidth(), first.getRegionHeight());
            }
            return;
        }

        // Cone gone → body takes damage
        health -= dmg;
        if (health <= 0) {
            isDying = true;
            stateTime = 0f;
        }
    }

    public void dispose() {
        walkConeSheet.dispose();
        eatConeSheet.dispose();

        walkNormalSheet.dispose();
        eatNormalSheet.dispose();
        dieNormalSheet.dispose();
    }
}
