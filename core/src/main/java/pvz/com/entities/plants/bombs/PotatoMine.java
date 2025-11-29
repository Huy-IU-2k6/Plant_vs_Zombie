package pvz.com.entities.plants.bombs;

import pvz.com.entities.plants.Plant;
import pvz.com.entities.components.*;

public class PotatoMine extends Plant {

    public PotatoMine(float x, float y, int col, int row) {
        // 1. Khung sườn (Nhỏ hơn bình thường chút)
        super(x, y, 70, 70);

        // 2. Hình ảnh ban đầu: CỤC ĐẤT (Unarmed)
        // Bạn cần 2 ảnh: "potatomine_dirt.png" và "potatomine_ready.gif"
        this.addComponent(new SpriteComponent("images/Plants/potatomine_dirt.png"));

        // 3. Máu (300 HP - Zombie có thể ăn lúc nó chưa lên)
        this.addComponent(new HealthComponent(300));

        // 4. Component quản lý việc trồi lên
        // 14 giây là chuẩn game, để 3 giây test cho nhanh
        this.addComponent(new ArmingComponent(3.0f)); 

        // 5. Định danh
        this.addComponent(new TeamComponent(Team.PLANT));
        this.addComponent(new GridPositionComponent(col, row));
        
        // 6. Dữ liệu nổ (Sát thương cực to 1800, phạm vi rất nhỏ 50f)
        // fuseTime = 0 vì đạp trúng là nổ ngay
        this.addComponent(new ExplosiveComponent(1800, 50f, 0f));
    }
}
