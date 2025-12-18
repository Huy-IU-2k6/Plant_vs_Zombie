package pvz.com.entities.plants.bombs;

import pvz.com.entities.plants.Plant;
import pvz.com.entities.components.*;
import pvz.com.factories.PlantAssetLoader;

public class CherryBomb extends Plant {

    public CherryBomb(float x, float y, int col, int row) {
        // Kích thước cố định 90x90
        super(x, y, 90, 90);

        // 1. Lấy Animations từ Loader
        var idleAnim = PlantAssetLoader.CHERRY_IDLE;
        var explodeAnim = PlantAssetLoader.CHERRY_EXPLODE;

        if (idleAnim == null) {
            System.err.println("Error: CHERRY_BOMB animations are null.");
            return;
        }

        // 2. Thiết lập Components
        this.addComponent(new SpriteComponent(idleAnim.getKeyFrame(0)));

        AnimationComponent animComp = new AnimationComponent();
        animComp.addAnimation(EntityState.IDLE, idleAnim);
        animComp.addAnimation(EntityState.EXPLODING, explodeAnim);
        this.addComponent(animComp);

        this.addComponent(new StateComponent(EntityState.IDLE));
        this.addComponent(new HealthComponent(300));
        this.addComponent(new TeamComponent(Team.PLANT));
        this.addComponent(new GridCellComponent(col, row));

        this.addComponent(new ExplosiveComponent(1800, 250f, 1.0f));
    }
}
