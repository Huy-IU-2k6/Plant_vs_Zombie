package pvz.com.entities.plants.defenders;

import pvz.com.entities.plants.Plant;
import pvz.com.entities.components.*;

public class Wallnut extends Plant {

    public Wallnut(float x, float y) {
        // 1. Khung sườn (Vị trí, kích thước 70x90 hoặc tùy chỉnh)
        super(x, y, 70, 90);

        // 2. Hình ảnh
        // Đảm bảo bạn đã có ảnh này trong thư mục assets
        this.addComponent(new SpriteComponent("assets/images/Plants/Wallnut.gif"));

        // 3. Máu rất cao (4000 HP)
        this.addComponent(new HealthComponent(4000));

        // LƯU Ý QUAN TRỌNG VỀ SOLID/ECS:
        // Wallnut KHÔNG thêm PlantAttackComponent -> System tấn công sẽ tự động bỏ qua nó.
        // Wallnut KHÔNG thêm SunProducerComponent -> System sinh sun sẽ tự động bỏ qua nó.
        // -> Không tốn tài nguyên CPU để kiểm tra logic thừa!
    }
}