package pvz.com.entities.plants.defenders;

import pvz.com.entities.plants.Plant;
import pvz.com.entities.components.*;

public class Wallnut extends Plant {

    // Nên truyền thêm gridCol, gridRow để xác định vị trí trên lưới
    public Wallnut(float x, float y, int gridCol, int gridRow) {
        // 1. Khung sườn cơ bản (Position, Size, Bounds - giả sử class cha Plant đã lo việc này)
        super(x, y, 70, 90); 

        // 2. Hình ảnh (Sprite)
        // Lưu ý: Về sau nên dùng AssetManager để load Texture tối ưu hơn
        this.addComponent(new SpriteComponent("assets/images/Plants/Wallnut.gif"));

        // 3. Chỉ số sinh tồn
        this.addComponent(new HealthComponent(4000)); // Máu trâu

        // --- CÁC COMPONENT MỚI CẦN THÊM ---

        // 4. Định danh phe phái (QUAN TRỌNG)
        // Nếu thiếu cái này, CollisionSystem sẽ không biết Wallnut là Cây hay Zombie
        this.addComponent(new TeamComponent(Team.PLANT));

        // 5. Vị trí trên lưới (QUAN TRỌNG)
        // Để Zombie biết có cây nào đang ở cùng làn (row) với nó không
        this.addComponent(new GridPositionComponent(gridCol, gridRow));

        // 6. Trạng thái (Tùy chọn nâng cao)
        // Wallnut có 3 dạng: Nguyên vẹn, Nứt nhẹ, Nứt to.
        // Ta thêm StateComponent để sau này thay đổi hình ảnh dựa theo % máu.
        this.addComponent(new StateComponent(EntityState.IDLE));

        // 7. Giá tiền (Tùy chọn)
        // Để hệ thống quản lý Sun biết trừ bao nhiêu tiền
        // Bạn có thể tạo thêm CostComponent như đã bàn, hoặc quản lý trong Enum PlantType
        // this.addComponent(new CostComponent(50)); 
    }
}