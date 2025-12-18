package pvz.com.entities.plants.shooters;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

import pvz.com.entities.plants.Plant;
import pvz.com.entities.components.*;
import pvz.com.entities.projectiles.PeaProjectile;
import pvz.com.managers.GridConfig;
import pvz.com.managers.DesignConfig;

public class Peashooter extends Plant {

    private static final float SCALE_X = 0.7f;
    private static final float SCALE_Y = 0.8f;

    // Tốc độ animation (0.1 giây / frame)
    // Số càng nhỏ cây lắc lư càng nhanh
    private static final float FRAME_DURATION = DesignConfig.FRAME_DURATION;

    public Peashooter(float x, float y, int col, int row) {
        super(
                x,
                y,
                GridConfig.CELL_WIDTH * SCALE_X,
                GridConfig.CELL_HEIGHT * SCALE_Y);

        // =============================================================
        // 1. TẠO ANIMATION TỪ CÁC FILE PNG RỜI
        // =============================================================
        Array<TextureRegion> frames = new Array<>();

        // Giả sử bạn có 13 frame (từ 0 đến 12).
        // Hãy sửa số 12 thành số frame thực tế bạn có trong thư mục assets.
        for (int i = 0; i <= 12; i++) {
            // Đường dẫn file ảnh: images/Plants/Peashooter/Peashooter_0.png
            Texture tex = new Texture("images/Plants/Peashooter/Peashooter_" + i + ".png");

            // TextureRegion là "lớp áo" bọc lấy Texture để Animation dùng được
            frames.add(new TextureRegion(tex));
        }

        // Tạo Animation lặp lại vô tận (LOOP)
        Animation<TextureRegion> idleAnim = new Animation<>(FRAME_DURATION, frames, Animation.PlayMode.LOOP);

        // =============================================================
        // 2. THIẾT LẬP CÁC COMPONENT (Để ECS Systems xử lý)
        // =============================================================

        // A. SpriteComponent: Bắt đầu bằng frame đầu tiên để vẽ ngay lập tức
        this.addComponent(new SpriteComponent(frames.first()));

        // B. AnimationComponent: Chứa dữ liệu animation (để AnimationSystem sử dụng)
        AnimationComponent animComp = new AnimationComponent();
        // Gán animation này cho trạng thái IDLE (Đứng yên)
        animComp.addAnimation(EntityState.IDLE, idleAnim);
        this.addComponent(animComp);

        // C. StateComponent: Quản lý thời gian chạy animation (stateTime)
        // Bắt buộc phải có cái này thì AnimationSystem mới tính giờ được
        this.addComponent(new StateComponent(EntityState.IDLE));

        // D. Các component chỉ số khác (Giữ nguyên)
        this.addComponent(new HealthComponent(100));
        this.addComponent(new TeamComponent(Team.PLANT));
        this.addComponent(new GridCellComponent(col, row));

        // E. Khả năng tấn công
        this.addComponent(new PlantAttackComponent(
                20, // damage
                900f, // range
                PeaProjectile.class,
                PlantDamageType.NORMAL,
                1.5f // cooldown
        ));
    }

    // Constructor phụ (để tương thích code cũ nếu cần)
    public Peashooter(float x, float y) {
        this(x, y, -1, -1);
    }
}
