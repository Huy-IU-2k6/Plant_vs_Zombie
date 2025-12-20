package pvz.com.entities.plants.producers;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import pvz.com.entities.plants.Plant;
import pvz.com.entities.components.*;
import pvz.com.managers.DesignConfig;
import pvz.com.managers.GridConfig;
import pvz.com.entities.factories.PlantAssetLoader;

public class SunFlower extends Plant {

    private static final float SCALE_X = 0.7f;
    private static final float SCALE_Y = 0.8f;

    public SunFlower(float x, float y, int col, int row) {
        super(
                x,
                y,
                GridConfig.CELL_WIDTH * SCALE_X,
                GridConfig.CELL_HEIGHT * SCALE_Y);


        var idleAnim = PlantAssetLoader.SUNFLOWER_IDLE;


        if (idleAnim == null) {
            System.err.println("CRITICAL ERROR: PlantAssetLoader.SUNFLOWER_IDLE is NULL. Did you call loadAll()?");
            return;
        }





        TextureRegion firstFrame = idleAnim.getKeyFrame(0);


        this.addComponent(new SpriteComponent(firstFrame));

        AnimationComponent animComp = new AnimationComponent();
        animComp.addAnimation(EntityState.IDLE, idleAnim);
        this.addComponent(animComp);

        this.addComponent(new StateComponent(EntityState.IDLE));
        this.addComponent(new HealthComponent(100));
        this.addComponent(new TeamComponent(Team.PLANT));
        this.addComponent(new GridCellComponent(col, row));
        this.addComponent(new SunProducerComponent(DesignConfig.SUN_COOL_DOWN, 25));
    }

    public SunFlower(float x, float y) {
        this(x, y, -1, -1);
    }
}
