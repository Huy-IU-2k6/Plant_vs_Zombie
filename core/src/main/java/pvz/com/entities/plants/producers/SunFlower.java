package pvz.com.entities.plants.producers;

import pvz.com.entities.plants.Plant;
import pvz.com.entities.components.*;

public class SunFlower extends Plant {

    public SunFlower(float x, float y) {
        // 1. Setup phần khung cơ bản (Vị trí, kích thước 80x80)
        super(x, y, 80, 80);

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
