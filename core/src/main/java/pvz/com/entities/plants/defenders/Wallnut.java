package pvz.com.entities.plants.defenders;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import pvz.com.entities.plants.Plant;
import pvz.com.entities.components.*;
import pvz.com.managers.GridConfig;
import pvz.com.factories.PlantAssetLoader;

public class Wallnut extends Plant {

    private static final float SCALE_X = 0.7f;
    private static final float SCALE_Y = 0.8f;
    private static final int TOTAL_HEALTH = 400;

    public Wallnut(float x, float y, int gridCol, int gridRow) {
        super(x, y, GridConfig.CELL_WIDTH * SCALE_X, GridConfig.CELL_HEIGHT * SCALE_Y);

        // 1. Lấy Animations từ Loader
        var animFull = PlantAssetLoader.WALLNUT_FULL;
        var animCrack1 = PlantAssetLoader.WALLNUT_CRACKED1;
        var animCrack2 = PlantAssetLoader.WALLNUT_CRACKED2;

        if (animFull == null) {
             System.err.println("Error: WALLNUT animations are null.");
             return;
        }

        // 2. Thiết lập Components
        // Frame đầu tiên (trạng thái Full)
        this.addComponent(new SpriteComponent(animFull.getKeyFrame(0)));

        AnimationComponent animComp = new AnimationComponent();
        animComp.addAnimation(EntityState.WALLNUT_FULL, animFull);
        animComp.addAnimation(EntityState.WALLNUT_CRACKED_1, animCrack1);
        animComp.addAnimation(EntityState.WALLNUT_CRACKED_2, animCrack2);
        this.addComponent(animComp);

        this.addComponent(new StateComponent(EntityState.WALLNUT_FULL));
        this.addComponent(new HealthComponent(TOTAL_HEALTH));
        this.addComponent(new TeamComponent(Team.PLANT));
        this.addComponent(new GridCellComponent(gridCol, gridRow));
    }

    public Wallnut(float x, float y) {
        this(x, y, -1, -1);
    }
}