package pvz.com.entities.plants.shooters;

import pvz.com.entities.plants.Plant;
import pvz.com.entities.components.*;
import pvz.com.entities.projectiles.PeaProjectile;
import pvz.com.managers.GridConfig;

public class Peashooter extends Plant {

    public Peashooter(float x, float y) {
        // 1. Setup khung sườn: Vị trí + Kích thước bằng đúng 1 ô (80 x 100)
        super(x, y, GridConfig.CELL_WIDTH, GridConfig.CELL_HEIGHT);

        // 2. Hình ảnh
        this.addComponent(new SpriteComponent("assets/images/Plants/peashooterani.gif"));

        // 3. Máu (100 HP)
        this.addComponent(new HealthComponent(100));

        // 4. Tấn công
        // CooldownComponent đã nằm TRONG PlantAttackComponent, không cần add riêng.
        this.addComponent(new PlantAttackComponent(
                20, // Damage
                300f, // Range
                PeaProjectile.class, // Loại đạn
                PlantDamageType.FIRE, // Type damage (NORMAL/FIRE tùy ông định nghĩa)
                1.5f // Cooldown bắn
        ));
    }
}
