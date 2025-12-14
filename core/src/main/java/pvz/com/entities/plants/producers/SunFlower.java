package pvz.com.entities.plants.producers;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

import pvz.com.entities.plants.Plant;
import pvz.com.entities.components.*;
import pvz.com.managers.GridConfig;

public class SunFlower extends Plant {

    // Scale kích thước
    private static final float SCALE_X = 0.7f;
    private static final float SCALE_Y = 0.8f;

    // Tốc độ animation (càng nhỏ càng nhanh)
    private static final float FRAME_DURATION = 0.12f;

    public SunFlower(float x, float y, int col, int row) {
        super(
                x,
                y,
                GridConfig.CELL_WIDTH * SCALE_X,
                GridConfig.CELL_HEIGHT * SCALE_Y);

        // =============================================================
        // 1. TẠO ANIMATION (Thay thế dòng load GIF cũ)
        // =============================================================
        Array<TextureRegion> frames = new Array<>();

        // Giả sử SunFlower có 18 frame (từ 0 đến 17).
        // Bạn hãy sửa số 17 thành số frame thực tế bạn có.
        for (int i = 0; i <= 17; i++) {
            // Đường dẫn: images/Plants/SunFlower/SunFlower_0.png
            Texture tex = new Texture("images/Plants/SunFlower/SunFlower_" + i + ".png");
            frames.add(new TextureRegion(tex));
        }

        // Tạo Animation lặp lại (LOOP)
        Animation<TextureRegion> idleAnim = new Animation<>(FRAME_DURATION, frames, Animation.PlayMode.LOOP);

        // =============================================================
        // 2. THIẾT LẬP COMPONENTS
        // =============================================================

        // A. Hình ảnh khởi đầu (Frame đầu tiên)
        this.addComponent(new SpriteComponent(frames.first()));

        // B. Animation Component (Chứa dữ liệu chuyển động)
        AnimationComponent animComp = new AnimationComponent();
        animComp.addAnimation(EntityState.IDLE, idleAnim);
        this.addComponent(animComp);

        // C. State Component (Để đếm giờ cho animation)
        this.addComponent(new StateComponent(EntityState.IDLE));

        // D. Các chỉ số khác (Máu, Phe, Vị trí...)
        this.addComponent(new HealthComponent(100));
        this.addComponent(new TeamComponent(Team.PLANT));
        this.addComponent(new GridCellComponent(col, row));

        // E. Sinh Sun (7 giây ra 1 lần, mỗi lần 25 sun)
        this.addComponent(new SunProducerComponent(7.0f, 25));
    }

    // Constructor phụ để tương thích code cũ
    public SunFlower(float x, float y) {
        this(x, y, -1, -1);
    }
}
