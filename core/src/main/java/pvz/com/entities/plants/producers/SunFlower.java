package pvz.com.entities.plants.producers;

import pvz.com.entities.plants.Plant;
import pvz.com.entities.components.*;

import pvz.com.managers.GridConfig;

public class SunFlower extends Plant {

    // scale so với kích thước ô – lấy từ bản của bạn
    private static final float SCALE_X = 0.7f; // rộng 70% ô
    private static final float SCALE_Y = 0.8f; // cao 80% ô

    // Constructor CHÍNH: dùng cho hệ thống grid + factory
    public SunFlower(float x, float y, int col, int row) {
        super(
                x,
                y,
                GridConfig.CELL_WIDTH * SCALE_X,
                GridConfig.CELL_HEIGHT * SCALE_Y);

        // Hình ảnh
        this.addComponent(new SpriteComponent("images/Plants/SunFlower.gif"));

        // Trạng thái (IDLE -> PRODUCE -> IDLE)
        this.addComponent(new StateComponent(EntityState.IDLE));

        // Trạng thái – để sau này làm nhiều state (idle, shine, v.v)
        this.addComponent(new StateComponent(EntityState.IDLE));

        // Máu
        this.addComponent(new HealthComponent(100));

        // Sinh sun: 7 giây/lần, 25 sun
        this.addComponent(new SunProducerComponent(7.0f, 25));

        // Phe: Plant
        this.addComponent(new TeamComponent(Team.PLANT));

        // Vị trí lưới
        this.addComponent(new GridPositionComponent(col, row));
    }

    // OPTIONAL: overload để không vỡ code cũ
    // Nếu vẫn còn chỗ nào gọi new SunFlower(x, y)
    public SunFlower(float x, float y) {
        this(x, y, -1, -1); // col/row “tạm”, nếu cần có thể xử lý đặc biệt sau
    }
}
