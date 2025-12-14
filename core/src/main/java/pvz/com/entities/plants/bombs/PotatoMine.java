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

    private static final float SCALE_X = 0.65f;
    private static final float SCALE_Y = 0.70f;

    private static final float FRAME_DURATION = 0.12f;

    private static final int TOTAL_HEALTH = 300;

    // PvZ chuẩn: 14s. Muốn test nhanh thì đổi thành 3f.
    private static final float ARMING_TIME = 14f;

    private static final int DAMAGE = 1800;
    private static final float RADIUS = 50f;
    private static final float FUSE_TIME = 0f; // dẫm là nổ ngay

    public PotatoMine(float x, float y, int col, int row) {
        super(
                x,
                y,
                GridConfig.CELL_WIDTH * SCALE_X,
                GridConfig.CELL_HEIGHT * SCALE_Y);

        // =========================================================
        // 1) LOAD ANIMATIONS CHO CÁC TRẠNG THÁI CHUẨN
        // =========================================================
        // Lưu ý: đường dẫn/prefix dưới đây là mẫu theo kiểu frame PNG:
        // prefix + i + ".png"
        // Bạn chỉnh lại đúng folder asset của bạn.

        Animation<TextureRegion> animUnarmed = loadAnimation(
                "images/Plants/PotatoMine/Unarmed/PotatoMine_unarmed_", 10, Animation.PlayMode.LOOP);

        Animation<TextureRegion> animArming = loadAnimation(
                "images/Plants/PotatoMine/Arming/PotatoMine_arming_", 10, Animation.PlayMode.LOOP);

        Animation<TextureRegion> animArmed = loadAnimation(
                "images/Plants/PotatoMine/Armed/PotatoMine_armed_", 10, Animation.PlayMode.LOOP);

        Animation<TextureRegion> animExplode = loadAnimation(
                "images/Plants/PotatoMine/Explode/PotatoMine_explode_", 10, Animation.PlayMode.NORMAL);

        // =========================================================
        // 2) COMPONENTS
        // =========================================================

        // A) SpriteComponent: bắt đầu ở UNARMED
        this.addComponent(new SpriteComponent(animUnarmed.getKeyFrame(0)));

        // B) AnimationComponent: map state -> animation
        AnimationComponent animComp = new AnimationComponent();
        animComp.addAnimation(EntityState.POTATOMINE_UNARMED, animUnarmed);
        animComp.addAnimation(EntityState.POTATOMINE_ARMING, animArming);
        animComp.addAnimation(EntityState.POTATOMINE_ARMED, animArmed);
        animComp.addAnimation(EntityState.POTATOMINE_EXPLODING, animExplode);
        this.addComponent(animComp);

        // C) StateComponent: state ban đầu
        this.addComponent(new StateComponent(EntityState.POTATOMINE_UNARMED));

        // D) Máu + team + vị trí ô
        this.addComponent(new HealthComponent(TOTAL_HEALTH));
        this.addComponent(new TeamComponent(Team.PLANT));
        this.addComponent(new GridCellComponent(col, row));

        // E) Arming + Explosive (giữ đúng logic cũ)
        this.addComponent(new ArmingComponent(ARMING_TIME));
        this.addComponent(new ExplosiveComponent(DAMAGE, RADIUS, FUSE_TIME));
    }

    public PotatoMine(float x, float y) {
        this(x, y, -1, -1);
    }

    private Animation<TextureRegion> loadAnimation(String prefixPath, int frameCount, Animation.PlayMode playMode) {
        Array<TextureRegion> frames = new Array<>();
        for (int i = 0; i < frameCount; i++) {
            Texture tex = new Texture(prefixPath + i + ".png");
            tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            frames.add(new TextureRegion(tex));
        }
        Animation<TextureRegion> anim = new Animation<>(FRAME_DURATION, frames);
        anim.setPlayMode(playMode);
        return anim;
    }
}
