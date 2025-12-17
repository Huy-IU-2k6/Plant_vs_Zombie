package pvz.com.entities.plants.bombs;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

import pvz.com.entities.components.AnimationComponent;
import pvz.com.entities.components.ArmingComponent;
import pvz.com.entities.components.EntityState;
import pvz.com.entities.components.ExplosiveComponent;
import pvz.com.entities.components.GridCellComponent;
import pvz.com.entities.components.HealthComponent;
import pvz.com.entities.components.SpriteComponent;
import pvz.com.entities.components.StateComponent;
import pvz.com.entities.components.Team;
import pvz.com.entities.components.TeamComponent;
import pvz.com.entities.plants.Plant;
import pvz.com.managers.GridConfig;

public class PotatoMine extends Plant {

    // ===== SIZE / ANIM =====
    private static final float SCALE_X = 0.65f;
    private static final float SCALE_Y = 0.70f;
    private static final float FRAME_DURATION = 0.12f;

    // ===== STATS =====
    private static final int MAX_HEALTH = 300;

    // PvZ chuẩn: 14s (test nhanh thì đổi nhỏ hơn)
    private static final float ARMING_TIME = 14f;

    private static final int DAMAGE = 1800;
    private static final float RADIUS = 50f;
    private static final float FUSE_TIME = 0f; // dẫm là nổ ngay

    // ===== ASSET PATHS (đặt tên rõ ràng hơn) =====
    private static final String PATH_UNARMED_PREFIX = "images/Plants/PotatoMine/PotatoMineInit/PotatoMineInit_"; // 1

    private static final String PATH_ARMED_PREFIX = "images/Plants/PotatoMine/PotatoMine/PotatoMine_"; // 8 frames:

    private static final String PATH_EXPLODE_PREFIX = "images/Plants/PotatoMine/PotatoMineExplode/PotatoMineExplode_"; // 1

    public PotatoMine(float x, float y, int col, int row) {
        super(
                x,
                y,
                GridConfig.CELL_WIDTH * SCALE_X,
                GridConfig.CELL_HEIGHT * SCALE_Y);

        // =========================================================
        // 1) LOAD FRAMES (giống style SunFlower: for-loop + Array)
        // =========================================================

        // Unarmed: 1 frame (0..0)
        Animation<TextureRegion> unarmedAnim = buildAnimation(
                PATH_UNARMED_PREFIX, 0, 0, Animation.PlayMode.LOOP);

        // Armed loop: 8 frames (0..7)
        Animation<TextureRegion> armedLoopAnim = buildAnimation(
                PATH_ARMED_PREFIX, 0, 7, Animation.PlayMode.LOOP);

        // Explode: 1 frame (0..0)
        Animation<TextureRegion> explodeAnim = buildAnimation(
                PATH_EXPLODE_PREFIX, 0, 0, Animation.PlayMode.NORMAL);

        // =========================================================
        // 2) COMPONENTS
        // =========================================================

        // Sprite start: UNARMED frame đầu
        this.addComponent(new SpriteComponent(unarmedAnim.getKeyFrame(0)));

        // Animation map: bỏ animArming dư (trùng armed)
        AnimationComponent animComp = new AnimationComponent();
        animComp.addAnimation(EntityState.POTATOMINE_UNARMED, unarmedAnim);

        // Nếu system vẫn dùng state POTATOMINE_ARMING, map chung vào armedLoopAnim luôn
        animComp.addAnimation(EntityState.POTATOMINE_ARMING, armedLoopAnim);
        animComp.addAnimation(EntityState.POTATOMINE_ARMED, armedLoopAnim);

        animComp.addAnimation(EntityState.POTATOMINE_EXPLODING, explodeAnim);
        this.addComponent(animComp);

        // State ban đầu
        this.addComponent(new StateComponent(EntityState.POTATOMINE_UNARMED));

        // Stats / team / cell
        this.addComponent(new HealthComponent(MAX_HEALTH));
        this.addComponent(new TeamComponent(Team.PLANT));
        this.addComponent(new GridCellComponent(col, row));

        // Logic giữ nguyên
        this.addComponent(new ArmingComponent(ARMING_TIME));
        this.addComponent(new ExplosiveComponent(DAMAGE, RADIUS, FUSE_TIME));
    }

    public PotatoMine(float x, float y) {
        this(x, y, -1, -1);
    }

    // =========================================================
    // Helpers: load frames bằng vòng lặp như SunFlower
    // =========================================================
    private Animation<TextureRegion> buildAnimation(
            String prefix, int startFrame, int endFrameInclusive, Animation.PlayMode playMode) {

        Array<TextureRegion> frames = new Array<>();

        for (int i = startFrame; i <= endFrameInclusive; i++) {
            Texture tex = new Texture(prefix + i + ".png");
            tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            frames.add(new TextureRegion(tex));
        }

        Animation<TextureRegion> anim = new Animation<>(FRAME_DURATION, frames, playMode);
        return anim;
    }
}
