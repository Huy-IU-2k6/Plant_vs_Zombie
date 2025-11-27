package pvz.com.Zombies;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

public class ConeheadZombie extends Zombies {

    private static final int BODY_HEALTH = 100;
    private static final int CONE_HEALTH = 200;
    private static final float MOVE_SPEED = 50f;

    private static final int FRAMES_PER_ROW = 1;
    private static final float WALK_FRAME_TIME = 0.20f;
    private static final float EAT_FRAME_TIME = 0.25f;
    private static final float DIE_FRAME_TIME = 0.20f;

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

    // State
    private float stateTime = 0f;
    private boolean isDying = false;
    private boolean isEating = false;
    private boolean killedByCherryBomb = false;

    private boolean coneOnHead = true;
    private int coneHealth = CONE_HEALTH;

    public ConeheadZombie() {
        super();

        this.health = BODY_HEALTH;
        this.speed = MOVE_SPEED;

        // Load textures (chỉnh path cho đúng với project của bạn)
        walkConeSheet = new Texture("ConeheadZombie.gif");
        eatConeSheet = new Texture("ConeheadZombie_Eat.gif");

        walkNormalSheet = new Texture("NormalZombieRun.gif");
        eatNormalSheet = new Texture("NormalZombieEat.gif");
        dieNormalSheet = new Texture("ZombieDie.gif");

        burntZombieSheet = new Texture("BurntZombie.gif");

        // Build animations
        walkConeAnim = createAnim(walkConeSheet, FRAMES_PER_ROW, WALK_FRAME_TIME, Animation.PlayMode.LOOP);
        eatConeAnim = createAnim(eatConeSheet, FRAMES_PER_ROW, EAT_FRAME_TIME, Animation.PlayMode.LOOP);

        walkNormalAnim = createAnim(walkNormalSheet, FRAMES_PER_ROW, WALK_FRAME_TIME, Animation.PlayMode.LOOP);
        eatNormalAnim = createAnim(eatNormalSheet, FRAMES_PER_ROW, EAT_FRAME_TIME, Animation.PlayMode.LOOP);
        dieNormalAnim = createAnim(dieNormalSheet, FRAMES_PER_ROW, DIE_FRAME_TIME, Animation.PlayMode.NORMAL);

        burntAnim = createAnim(burntZombieSheet, FRAMES_PER_ROW, DIE_FRAME_TIME, Animation.PlayMode.NORMAL);

        // Set actor size to first cone frame
        TextureRegion first = walkConeAnim.getKeyFrame(0f);
        setSize(first.getRegionWidth(), first.getRegionHeight());
    }

    private Animation<TextureRegion> createAnim(Texture sheet,
            int frameCount,
            float frameDuration,
            Animation.PlayMode playMode) {

        int frameWidth = sheet.getWidth() / frameCount;
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
        // Đang trong animation chết → chỉ chạy anim, không dùng logic ở Zombies.act
        if (isDying) {
            stateTime += delta;

            Animation<TextureRegion> currentDieAnim = killedByCherryBomb ? burntAnim : dieNormalAnim;

            if (currentDieAnim.isAnimationFinished(stateTime)) {
                // Kết thúc: đánh dấu chết, trừ số lượng và remove actor
                dead = true;
                speed = 0f;
                if (zombieCount > 0) {
                    zombieCount--;
                }
                remove();
            }
            return;
        }

        // Cập nhật trạng thái đang ăn hay không
        boolean touching = isTouchingPlant();
        if (touching != isEating) {
            isEating = touching;
            stateTime = 0f;
        }

        // Gọi logic chung: di chuyển, sound, gameOver, v.v.
        super.act(delta);

        // Cập nhật thời gian cho animation
        stateTime += delta;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
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

    @Override
    public void takeDamage(int dmg) {
        if (isDying || dead)
            return;

        if (coneOnHead) {
            coneHealth -= dmg;

            if (coneHealth <= 0) {
                coneOnHead = false;
                stateTime = 0f;

                TextureRegion first = walkNormalAnim.getKeyFrame(0f);
                setSize(first.getRegionWidth(), first.getRegionHeight());
            }
            return;
        }

        // Normal damage
        health -= dmg;
        if (health <= 0) {
            isDying = true;
            stateTime = 0f;
        }
    }

    @Override
    public void killByCherryBomb() {
        if (isDying || dead)
            return;
        isDying = true;
        killedByCherryBomb = true;
        stateTime = 0f;
    }

    @Override
    public boolean isEating() {
        return isEating;
    }

    // Tạm thời: luôn trả về false, bạn thay bằng logic va chạm plant thật sau
    private boolean isTouchingPlant() {
        return false;
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
