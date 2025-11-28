package pvz.com.entities.plants.defenders;

import pvz.com.entities.plants.Plant;
import pvz.com.entities.components.*;
import pvz.com.managers.GridConfig;

public class Wallnut extends Plant {
    private static final float SCALE_X = 0.7f; // rộng 70% ô
    private static final float SCALE_Y = 0.8f; // cao 80% ô

    public Wallnut(float x, float y) {
        // 1. Khung sườn: Vị trí + kích thước đúng 1 ô (80 x 100)
        super(
                x,
                y,
                GridConfig.CELL_WIDTH * SCALE_X,
                GridConfig.CELL_HEIGHT * SCALE_Y);

        // 2. Hình ảnh
        this.addComponent(new SpriteComponent("assets/images/Plants/Wallnut.gif"));

        // 3. Máu rất cao (4000 HP)
        this.addComponent(new HealthComponent(4000));

        // Không có Attack / SunProducer -> System sẽ auto bỏ qua
    }
}
