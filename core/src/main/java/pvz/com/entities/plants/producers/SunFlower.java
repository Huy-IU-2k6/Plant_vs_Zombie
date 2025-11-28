package pvz.com.entities.plants.producers;

import pvz.com.entities.plants.Plant;
import pvz.com.entities.components.*;
import pvz.com.managers.GridConfig;

public class SunFlower extends Plant {
    private static final float SCALE_X = 0.7f; // rộng 70% ô
    private static final float SCALE_Y = 0.8f; // cao 80% ô

    public SunFlower(float x, float y) {
        // 1. Setup phần khung cơ bản (Vị trí, kích thước bằng đúng 1 ô: 80 x 100)
        super(
                x,
                y,
                GridConfig.CELL_WIDTH * SCALE_X,
                GridConfig.CELL_HEIGHT * SCALE_Y);

        // 2. Setup các Component ĐẶC TRƯNG (Data composition)

        // Hình ảnh hiển thị
        this.addComponent(new SpriteComponent("assets/images/Plants/sunflowerani.gif"));

        // Máu (100 HP)
        this.addComponent(new HealthComponent(100));

        // Khả năng sinh Sun: 7 giây một lần, mỗi lần 25 điểm
        // Class này CHỈ chứa số liệu, KHÔNG chứa logic đếm giờ (Logic thuộc về System)
        this.addComponent(new SunProducerComponent(7.0f, 25));
    }
}
