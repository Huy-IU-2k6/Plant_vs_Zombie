package pvz.com.entities.plants.shooters;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import pvz.com.entities.plants.Plant;
import pvz.com.entities.components.*;
import pvz.com.entities.projectiles.PeaProjectile;
import pvz.com.managers.GridConfig;
import pvz.com.managers.DesignConfig;
import pvz.com.factories.PlantAssetLoader;

public class Peashooter extends Plant {

    private static final float SCALE_X = 0.7f;
    private static final float SCALE_Y = 0.8f;

    // Tốc độ animation (0.1 giây / frame)
    // Số càng nhỏ cây lắc lư càng nhanh
    private static final float FRAME_DURATION = DesignConfig.FRAME_DURATION;

    public Peashooter(float x, float y, int col, int row) {
        super(
                x,
                y,
                GridConfig.CELL_WIDTH * SCALE_X,
                GridConfig.CELL_HEIGHT * SCALE_Y);

        // 1. Lấy Animation từ Loader
        var idleAnim = PlantAssetLoader.PEASHOOTER_IDLE;

        if (idleAnim == null) {
            System.err.println("Error: PEASHOOTER_IDLE is null. Call loadAll() first!");
            return;
        }

        // 2. Thiết lập Components
        // Dùng getKeyFrame(0) để lấy frame đầu tiên (tránh lỗi ClassCastException)
        this.addComponent(new SpriteComponent(idleAnim.getKeyFrame(0)));

        AnimationComponent animComp = new AnimationComponent();
        animComp.addAnimation(EntityState.IDLE, idleAnim);
        this.addComponent(animComp);

        this.addComponent(new StateComponent(EntityState.IDLE));
        this.addComponent(new HealthComponent(100));
        this.addComponent(new TeamComponent(Team.PLANT));
        this.addComponent(new GridCellComponent(col, row));

        this.addComponent(new PlantAttackComponent(
                20, 
                900f, 
                PeaProjectile.class, 
                PlantDamageType.NORMAL, 
                1.5f 
        ));
    }

    public Peashooter(float x, float y) {
        this(x, y, -1, -1);
    }
}