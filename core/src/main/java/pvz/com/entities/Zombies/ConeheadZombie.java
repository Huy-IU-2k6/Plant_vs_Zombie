package pvz.com.entities.Zombies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import pvz.com.managers.GifManager;

public class ConeheadZombie extends Zombies {

    private static final int BODY_HEALTH = 100;
    private static final int CONE_HEALTH = 200;
    private static final float MOVE_SPEED = 50f;

    private static final int FRAMES_PER_ROW = 1;
    private static final float WALK_FRAME_TIME = 0.20f;
    private static final float EAT_FRAME_TIME = 0.25f;
    private static final float DIE_FRAME_TIME = 0.20f;

    private static final float DESIRED_HEIGHT = 120f;

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

        // Scale chiều cao giống NormalZombie
        TextureRegion first = walkConeAnim.getKeyFrame(0f);
        float originalW = first.getRegionWidth();
        float originalH = first.getRegionHeight();

        float scale = DESIRED_HEIGHT / originalH;
        float desiredWidth = originalW * scale;

        setSize(desiredWidth, DESIRED_HEIGHT);
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
                float originalW = first.getRegionWidth();
                float originalH = first.getRegionHeight();

                float scale = DESIRED_HEIGHT / originalH;
                float desiredWidth = originalW * scale;

                setSize(desiredWidth, DESIRED_HEIGHT);
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
