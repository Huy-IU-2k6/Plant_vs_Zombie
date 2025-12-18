package pvz.com.entities.plants.shooters;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

import pvz.com.entities.plants.Plant;
import pvz.com.entities.components.*;
import pvz.com.entities.projectiles.FrozenPeaProjectile;
import pvz.com.managers.GridConfig;
import pvz.com.managers.DesignConfig;

public class SnowPea extends Plant {
    private static final float SCALE_X = 0.7f;
    private static final float SCALE_Y = 0.8f;

    // Số lượng frame ảnh bạn có (Ví dụ bạn có 13 ảnh từ 0 đến 12)
    private static final int FRAME_COUNT = 15;
    private static final float FRAME_DURATION = DesignConfig.FRAME_DURATION; // Tốc độ chạy ảnh

    public SnowPea(float x, float y, int col, int row) {
        // Kích thước hitbox (giữ nguyên logic scale cũ của bạn)
        super(x, y, GridConfig.CELL_WIDTH * SCALE_X, GridConfig.CELL_HEIGHT * SCALE_Y);

        // =============================================================
        // 1. LOAD ANIMATION FRAMES
        // =============================================================
        Array<TextureRegion> frames = new Array<>();
        // Giả sử ảnh tên là: SnowPea_0.png -> SnowPea_12.png
        // Bạn cần tạo folder: assets/images/Plants/SnowPea/
        for (int i = 0; i < FRAME_COUNT; i++) {
            Texture tex = new Texture("images/Plants/SnowPea/SnowPea_" + i + ".png");
            tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear); // Làm mịn ảnh
            frames.add(new TextureRegion(tex));
        }

        Animation<TextureRegion> idleAnim = new Animation<>(FRAME_DURATION, frames, Animation.PlayMode.LOOP);

        // =============================================================
        // 2. ADD COMPONENTS
        // =============================================================

        // SpriteComponent: Lấy frame đầu tiên làm hình đại diện ban đầu
        this.addComponent(new SpriteComponent(frames.first()));

        // AnimationComponent: Quản lý chuyển động
        AnimationComponent animComp = new AnimationComponent();
        animComp.addAnimation(EntityState.IDLE, idleAnim);
        // SnowPea thường dùng chung animation lắc lư cho cả lúc đứng yên và lúc bắn
        animComp.addAnimation(EntityState.ATTACKING, idleAnim);
        this.addComponent(animComp);

        // StateComponent: Bắt đầu ở trạng thái IDLE
        this.addComponent(new StateComponent(EntityState.IDLE));

        this.addComponent(new HealthComponent(100));
        this.addComponent(new TeamComponent(Team.PLANT));
        this.addComponent(new GridCellComponent(col, row));

        // Cấu hình bắn: Damage 20, Loại ICE, Đạn FrozenPeaProjectile
        this.addComponent(new PlantAttackComponent(
                20,
                900f,
                FrozenPeaProjectile.class,
                PlantDamageType.ICE,
                1.5f));
    }
}
