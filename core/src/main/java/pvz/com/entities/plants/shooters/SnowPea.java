package pvz.com.entities.plants.shooters;

import pvz.com.entities.plants.Plant;
import pvz.com.entities.components.*;
import pvz.com.entities.projectiles.FrozenPeaProjectile;
import pvz.com.managers.GridConfig;
import pvz.com.factories.PlantAssetLoader;

public class SnowPea extends Plant {
    private static final float SCALE_X = 0.7f;
    private static final float SCALE_Y = 0.8f;

    public SnowPea(float x, float y, int col, int row) {
        super(x, y, GridConfig.CELL_WIDTH * SCALE_X, GridConfig.CELL_HEIGHT * SCALE_Y);

        // 1. Lấy Animation từ Loader
        var idleAnim = PlantAssetLoader.SNOWPEA_IDLE;

        if (idleAnim == null) {
            System.err.println("Error: SNOWPEA_IDLE is null.");
            return;
        }

        // 2. Thiết lập Components
        this.addComponent(new SpriteComponent(idleAnim.getKeyFrame(0)));

        AnimationComponent animComp = new AnimationComponent();
        animComp.addAnimation(EntityState.IDLE, idleAnim);
        // SnowPea thường dùng chung animation lắc lư cho cả lúc đứng yên và lúc bắn
        animComp.addAnimation(EntityState.ATTACKING, idleAnim);

        // Dùng chung animation cho lúc bắn
        animComp.addAnimation(EntityState.ATTACKING, idleAnim);

        this.addComponent(animComp);

        this.addComponent(new StateComponent(EntityState.IDLE));
        this.addComponent(new HealthComponent(100));
        this.addComponent(new TeamComponent(Team.PLANT));
        this.addComponent(new GridCellComponent(col, row));

        this.addComponent(new PlantAttackComponent(
                30,
                900f,
                FrozenPeaProjectile.class,
                PlantDamageType.ICE,
                1.5f));
    }
}
