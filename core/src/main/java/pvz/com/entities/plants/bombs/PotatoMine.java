package pvz.com.entities.plants.bombs;

import pvz.com.entities.plants.Plant;
import pvz.com.entities.components.*;
import pvz.com.managers.GridConfig;
import pvz.com.factories.PlantAssetLoader;

public class PotatoMine extends Plant {

    public static final float BIG_WIDTH = GridConfig.CELL_WIDTH * 0.8f;
    public static final float BIG_HEIGHT = GridConfig.CELL_HEIGHT * 0.8f;

    public static final float SMALL_WIDTH = GridConfig.CELL_WIDTH * 0.4f;
    public static final float SMALL_HEIGHT = GridConfig.CELL_HEIGHT * 0.4f;

    public PotatoMine(float x, float y, int col, int row) {
        // Khởi tạo với kích thước NHỎ
        super(x, y, SMALL_WIDTH, SMALL_HEIGHT);

        // Căn giữa ô
        float centerX = GridConfig.getCellCenterX(col);
        float centerY = GridConfig.getCellCenterY(row);
        PositionComponent pos = this.getComponent(PositionComponent.class);
        if (pos != null) {
            pos.x = centerX - (SMALL_WIDTH / 2f);
            pos.y = centerY - (SMALL_HEIGHT / 2f);
        }

        // 1. Lấy Animations từ Loader
        var growAnim = PlantAssetLoader.POTATO_GROWING;
        var unarmedAnim = PlantAssetLoader.POTATO_UNARMED;
        var riseAnim = PlantAssetLoader.POTATO_RISING;
        var idleAnim = PlantAssetLoader.POTATO_IDLE;
        var explodeAnim = PlantAssetLoader.POTATO_EXPLODE;

        if (growAnim == null) {
             System.err.println("Error: POTATO_MINE animations are null.");
             return;
        }

        // 2. Thiết lập Components
        AnimationComponent animComp = new AnimationComponent();
        animComp.addAnimation(EntityState.GROWING, growAnim);
        animComp.addAnimation(EntityState.UNARMED, unarmedAnim);
        animComp.addAnimation(EntityState.RISING, riseAnim);
        animComp.addAnimation(EntityState.IDLE, idleAnim);
        animComp.addAnimation(EntityState.EXPLODING, explodeAnim);
        this.addComponent(animComp);

        // Frame đầu tiên (trạng thái Growing)
        this.addComponent(new SpriteComponent(growAnim.getKeyFrame(0)));
        this.addComponent(new StateComponent(EntityState.GROWING));

        this.addComponent(new HealthComponent(300));
        this.addComponent(new ArmingComponent(3.0f)); 
        this.addComponent(new TeamComponent(Team.PLANT));
        this.addComponent(new GridCellComponent(col, row));
        
        this.addComponent(new ExplosiveComponent(1800, 150f, -1f));
    }
}
