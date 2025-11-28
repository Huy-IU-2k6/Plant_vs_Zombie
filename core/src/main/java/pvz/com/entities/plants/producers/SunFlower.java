package pvz.com.entities.plants.producers;

import pvz.com.entities.plants.Plant;
import pvz.com.entities.components.*;

public class SunFlower extends Plant {

    // Thêm tham số col, row vào constructor để xác định vị trí lưới
    public SunFlower(float x, float y, int col, int row) {
        // 1. Setup khung cơ bản (Vị trí thực, kích thước 80x80)
        super(x, y, 80, 80);

        // 2. Setup hình ảnh & Animation
        // Lưu ý: SunFlower thường có animation đung đưa, nên dùng AnimationComponent sẽ đẹp hơn Sprite tĩnh về sau.
        this.addComponent(new SpriteComponent("assets/images/Plants/sunflowerani.gif"));
        
        // Thêm State để sau này có thể chuyển trạng thái (VD: IDLE -> Sắp đẻ Sun -> IDLE)
        this.addComponent(new StateComponent(EntityState.IDLE));

        // 3. Chỉ số sinh tồn
        this.addComponent(new HealthComponent(100)); // Máu giấy hơn Wallnut

        // 4. Khả năng sinh Sun (DATA ONLY)
        // 7 giây/lần, 25 sun.
        this.addComponent(new SunProducerComponent(7.0f, 25));

        // --- CÁC COMPONENT QUAN TRỌNG MỚI THÊM ---

        // 5. Định danh phe (Để Zombie biết mà ăn)
        this.addComponent(new TeamComponent(Team.PLANT));

        // 6. Vị trí trên lưới (Để hệ thống quản lý ô đất biết ô này đã có cây)
        this.addComponent(new GridPositionComponent(col, row));
    }
}