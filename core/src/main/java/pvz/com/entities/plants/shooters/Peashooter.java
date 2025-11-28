package pvz.com.entities.plants.shooters;

import pvz.com.entities.plants.Plant;
import pvz.com.entities.components.*;
// Đảm bảo bạn sẽ tạo class này ở bước sau
import pvz.com.entities.projectiles.PeaProjectile;

public class Peashooter extends Plant {

    // Thêm col, row vào constructor
    public Peashooter(float x, float y, int col, int row) {
        // 1. Setup khung sườn: Vị trí + Kích thước (80x80)
        super(x, y, 80, 80);

        // 2. Setup Hình ảnh & Trạng thái
        this.addComponent(new SpriteComponent("assets/images/Plants/peashooterani.gif"));
        this.addComponent(new StateComponent(EntityState.IDLE)); // Mặc định đứng yên

        // 3. Setup Máu (100 HP)
        this.addComponent(new HealthComponent(100));

        // 4. Các Component định danh (QUAN TRỌNG CHO LOGIC BẮN)
        
        // Phe Plant (Để Zombie biết mà ăn)
        this.addComponent(new TeamComponent(Team.PLANT));
        
        // Vị trí làn (Để súng biết làn này có Zombie không mà bắn)
        this.addComponent(new GridPositionComponent(col, row));

        // 5. Setup Tấn công
        this.addComponent(new PlantAttackComponent(
            20,                     // Damage
            900f,                   // Range: Thường là chiều ngang màn hình (Game PvZ bắn vô tận)
            PeaProjectile.class,    // Class đạn sẽ sinh ra
            PlantDamageType.NORMAL, // Loại đạn (Đã sửa thành NORMAL)
            1.5f                    // Cooldown: 1.5 giây bắn 1 viên
        ));
        this.addComponent(new TeamComponent(Team.PLANT));
    }
}