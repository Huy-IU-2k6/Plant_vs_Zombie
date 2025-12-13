// File: SnowPea.java
package pvz.com.entities.plants.shooters;

import pvz.com.entities.plants.Plant;
import pvz.com.entities.components.*;
import pvz.com.entities.projectiles.FrozenPeaProjectile;
import pvz.com.managers.GridConfig;

public class SnowPea extends Plant {
    private static final float SCALE_X = 0.7f;
    private static final float SCALE_Y = 0.8f;

    public SnowPea(float x, float y, int col, int row) {
        super(x, y, GridConfig.CELL_WIDTH * SCALE_X, GridConfig.CELL_HEIGHT * SCALE_Y);

        this.addComponent(new SpriteComponent("images/Plants/SnowPea.png")); // Ảnh cây xanh
        this.addComponent(new StateComponent(EntityState.IDLE));
        this.addComponent(new HealthComponent(100));
        this.addComponent(new TeamComponent(Team.PLANT));
        this.addComponent(new GridCellComponent(col, row));

        // Cấu hình bắn: Damage 20, Loại ICE, Đạn FrozenPeaProjectile
        this.addComponent(new PlantAttackComponent(
                20,
                900f,
                FrozenPeaProjectile.class, // <--- Class đạn mới
                PlantDamageType.ICE, // <--- Hệ băng
                1.5f));
    }
}
