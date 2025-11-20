package pvz.com.entities.plants.shooters;

import pvz.com.entities.plants.Plant;
import pvz.com.entities.components.*;
// Giả sử bạn sẽ tạo class PeaProjectile cho viên đạn
import pvz.com.entities.projectiles.PeaProjectile; 

public class Peashooter extends Plant {

    public Peashooter(float x, float y) {
        // 1. Setup khung sườn: Vị trí + Kích thước (80x80)
        super(x, y, 80, 80);

        // 2. Setup Hình ảnh
        this.addComponent(new SpriteComponent("plants/shooters/peashooter.png"));

        // 3. Setup Máu (100 HP)
        this.addComponent(new HealthComponent(100));

        // 4. Setup Tấn công
        // Lưu ý: CooldownComponent đã nằm TRONG PlantAttackComponent rồi, không cần add lẻ bên ngoài.
        this.addComponent(new PlantAttackComponent(
            20,                     // Damage (Sát thương)
            300f,                   // Range (Tầm bắn)
            PeaProjectile.class,    // Loại đạn (Bắn ra viên đạn, không phải bắn ra cái cây)
            PlantDamageType.FIRE,   // Loại damage (Bạn để FIRE cũng được, hoặc sửa thành NORMAL)
            1.5f                    // Tốc độ bắn (Cooldown)
        ));
    }
    
    // Đã XÓA hết các hàm getBaseHealth, getCooldownTime... vì không cần thiết nữa.
}