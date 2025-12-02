package pvz.com.entities.plants.shooters;

import pvz.com.entities.plants.Plant;
import pvz.com.entities.components.*;
import pvz.com.entities.projectiles.PeaProjectile;
import pvz.com.managers.GridConfig;

public class Peashooter extends Plant {

    private static final float SCALE_X = 0.7f; // rộng 70% ô
    private static final float SCALE_Y = 0.8f; // cao 80% ô

    // Constructor CHÍNH: có col, row để chơi với grid
    public Peashooter(float x, float y, int col, int row) {
        super(
                x,
                y,
                GridConfig.CELL_WIDTH * SCALE_X,
                GridConfig.CELL_HEIGHT * SCALE_Y);

        // Hình ảnh
        this.addComponent(new SpriteComponent("assets/images/Plants/Peashooter.gif"));

        // Trạng thái mặc định
        this.addComponent(new StateComponent(EntityState.IDLE));

        // Máu
        this.addComponent(new HealthComponent(100));

        // Phe
        this.addComponent(new TeamComponent(Team.PLANT));

        // Vị trí trên lưới
        this.addComponent(new GridPositionComponent(col, row));

        // Tấn công
        this.addComponent(new PlantAttackComponent(
                20, // damage
                900f, // range: gần như hết lane
                PeaProjectile.class, // loại đạn
                PlantDamageType.NORMAL, // Peashooter thường
                1.5f // cooldown
        ));
    }

    // OPTIONAL: để không vỡ code cũ nếu còn chỗ gọi Peashooter(x, y)
    public Peashooter(float x, float y) {
        this(x, y, -1, -1);
    }
}
