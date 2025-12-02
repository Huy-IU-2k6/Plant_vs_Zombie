package pvz.com.entities.plants.bombs;

import pvz.com.entities.plants.Plant;
import pvz.com.entities.components.*;

public class CherryBomb extends Plant {

    public CherryBomb(float x, float y, int col, int row) {
        // 1. Khung sườn (Vị trí thực tế, Kích thước 90x90 - to hơn cây thường chút)
        super(x, y, 90, 90);

        // 2. Hình ảnh
        this.addComponent(new SpriteComponent("images/Plants/CherryBomb.gif"));

        // 3. Trạng thái (Bắt đầu là IDLE, hệ thống sẽ chuyển sang EXPLODING khi hết
        // giờ)
        this.addComponent(new StateComponent(EntityState.IDLE));

        // 4. Máu (Vẫn cần máu để Zombie có thể ăn nó trong lúc nó đang chờ nổ)
        this.addComponent(new HealthComponent(300));

        // 5. Định danh & Vị trí (Quan trọng)
        this.addComponent(new TeamComponent(Team.PLANT));
        this.addComponent(new GridPositionComponent(col, row));

        // 6. Cơ chế Nổ (ĐẶC BIỆT)
        // - Damage: 1800 (One-shot hầu hết Zombie thường và Nón)
        // - Range: 150f (Tương đương bán kính 3x3 ô)
        // - FuseTime: 1.2s (Thời gian chờ animation "POW" trước khi gây damage)
        this.addComponent(new ExplosiveComponent(1800, 150f, 1.2f));
    }
}
