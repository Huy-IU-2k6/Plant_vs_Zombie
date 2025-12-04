package pvz.com.entities.Zombies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import pvz.com.managers.GifManager;

public class NormalZombie extends Zombies {

    // ===== CONST =====
    private static final int BODY_HEALTH = 100;
    private static final float MOVE_SPEED = 12f;
    private static final int FRAMES_PER_ROW = 1;
    private static final float WALK_FRAME_TIME = 0.20f;
    private static final float DIE_FRAME_TIME = 0.20f;
    private static final float EAT_FRAME_TIME = 0.25f;

    // Chiều cao zombie mong muốn trong world (vừa 1 ô cỏ ~ 100)
    private static final float DESIRED_HEIGHT = 120f;

    // Spritesheets
    private final Texture walkSheet;
    private final Texture dieSheet;
    private final Texture eatSheet;

    // Animations
    private final Animation<TextureRegion> walkAnim;
    private final Animation<TextureRegion> dieAnim;
    private final Animation<TextureRegion> eatAnim;

    // State
    private float stateTime = 0f;
    private boolean isDying = false;
    private boolean isEating = false;

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

        // ===== Set size với scale thay vì size gốc =====
        TextureRegion firstFrame = walkAnim.getKeyFrame(0f);
        float originalW = firstFrame.getRegionWidth();
        float originalH = firstFrame.getRegionHeight();

        float scale = DESIRED_HEIGHT / originalH;
        float desiredWidth = originalW * scale;

        setSize(desiredWidth, DESIRED_HEIGHT);

        // ===== Health & speed =====
        this.health = BODY_HEALTH;
        this.speed = MOVE_SPEED;
    }

    @Override
    public void act(float delta) {
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

        // Không tự xử lý isEating ở đây nữa.
        // CollisionSystem sẽ gọi setEating(true/false).
        super.act(delta);

        // Cập nhật thời gian animation
        stateTime += delta;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        // Nếu đã chết hẳn (animation kết thúc và đã set dead = true) thì khỏi vẽ
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

    /** Bắt đầu trạng thái chết (dùng chung cho đạn, mower, cherry bomb). */
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
        // Mower cán cũng cho chơi animation chết
        startDeath();
    }

    @Override
    public void killByCherryBomb() {
        // Sau này nếu có Burnt_Zombie.gif thì có thể đổi animation ở đây
        startDeath();
    }

    @Override
    public boolean isEating() {
        return isEating;
    }

    public void setEating(boolean eating) {
        if (isDying || dead)
            return;

        if (this.isEating == eating) {
            return; // không đổi gì thì thôi
        }

        this.isEating = eating;
        stateTime = 0f; // reset lại anim để chuyển mượt hơn

        if (eating) {
            // Đứng lại, để Zombies.act() không moveBy nữa
            this.speed = 0f;
        } else {
            // Đi tiếp như bình thường
            this.speed = MOVE_SPEED;
        }
    }

    public void dispose() {
        walkSheet.dispose();
        dieSheet.dispose();
        eatSheet.dispose();
    }
}
