package pvz.com.entities.plants.producers;

import pvz.com.entities.plants.Plant;
import pvz.com.entities.components.*;
import pvz.com.managers.GridConfig;

public class SunFlower extends Plant {
    // Tỉ lệ kích thước so với ô đất (để chừa khoảng trống cho đẹp)
    private static final float SCALE_X = 0.7f; 
    private static final float SCALE_Y = 0.8f; 

    // QUAN TRỌNG: Phải thêm int col, int row vào đây
    public SunFlower(float x, float y, int col, int row) {
        // 1. Setup phần khung cơ bản
        // Tính toán kích thước dựa trên GridConfig
        super(
            x + (GridConfig.CELL_WIDTH * (1 - SCALE_X) / 2), // Căn giữa theo X
            y + (GridConfig.CELL_HEIGHT * (1 - SCALE_Y) / 2), // Căn giữa theo Y
            GridConfig.CELL_WIDTH * SCALE_X,
            GridConfig.CELL_HEIGHT * SCALE_Y
        );

        // 2. Setup hình ảnh & Animation
        this.addComponent(new SpriteComponent("assets/images/Plants/sunflowerani.gif"));
        
        // Trạng thái (IDLE -> PRODUCE -> IDLE)
        this.addComponent(new StateComponent(EntityState.IDLE));

        // 3. Chỉ số sinh tồn
        this.addComponent(new HealthComponent(100));

        // 4. Khả năng sinh Sun (7 giây/lần, 25 sun)
        this.addComponent(new SunProducerComponent(7.0f, 25));

        // 5. Định danh phe
        this.addComponent(new TeamComponent(Team.PLANT));

        // 6. Vị trí trên lưới (Đã có biến col, row từ tham số để truyền vào)
        this.addComponent(new GridPositionComponent(col, row));
    }
}