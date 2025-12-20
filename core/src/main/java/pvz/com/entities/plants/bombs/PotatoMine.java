package pvz.com.entities.plants.bombs;

import pvz.com.entities.plants.Plant;
import pvz.com.managers.GridConfig;
import pvz.com.entities.factories.PlantAssetLoader;
import pvz.com.entities.components.state.EntityState;
import pvz.com.entities.components.state.StateComponent;
import pvz.com.entities.components.animation.AnimationComponent;
import pvz.com.entities.components.team.Team;
import pvz.com.entities.components.team.TeamComponent;
import pvz.com.entities.components.grid.GridCellComponent;
import pvz.com.entities.components.render.SpriteComponent;
import pvz.com.entities.components.physics.PositionComponent;
import pvz.com.entities.components.combat.HealthComponent;
import pvz.com.entities.components.combat.ArmingComponent;
import pvz.com.entities.components.combat.ExplosiveComponent;

public class PotatoMine extends Plant {

    public static final float BIG_WIDTH = GridConfig.CELL_WIDTH * 0.8f;
    public static final float BIG_HEIGHT = GridConfig.CELL_HEIGHT * 0.8f;

    public static final float SMALL_WIDTH = GridConfig.CELL_WIDTH * 0.4f;
    public static final float SMALL_HEIGHT = GridConfig.CELL_HEIGHT * 0.4f;

    public PotatoMine(float x, float y, int col, int row) {

        super(x, y, SMALL_WIDTH, SMALL_HEIGHT);

        float centerX = GridConfig.getCellCenterX(col);
        float centerY = GridConfig.getCellCenterY(row);
        PositionComponent pos = this.getComponent(PositionComponent.class);
        if (pos != null) {
            pos.x = centerX - (SMALL_WIDTH / 2f);
            pos.y = centerY - (SMALL_HEIGHT / 2f);
        }

        var growAnim = PlantAssetLoader.POTATO_GROWING;
        var unarmedAnim = PlantAssetLoader.POTATO_UNARMED;
        var riseAnim = PlantAssetLoader.POTATO_RISING;
        var idleAnim = PlantAssetLoader.POTATO_IDLE;
        var explodeAnim = PlantAssetLoader.POTATO_EXPLODE;

        if (growAnim == null) {
            System.err.println("Error: POTATO_MINE animations are null.");
            return;
        }

        AnimationComponent animComp = new AnimationComponent();
        animComp.addAnimation(EntityState.GROWING, growAnim);
        animComp.addAnimation(EntityState.UNARMED, unarmedAnim);
        animComp.addAnimation(EntityState.RISING, riseAnim);
        animComp.addAnimation(EntityState.IDLE, idleAnim);
        animComp.addAnimation(EntityState.EXPLODING, explodeAnim);
        this.addComponent(animComp);

        this.addComponent(new SpriteComponent(growAnim.getKeyFrame(0)));
        this.addComponent(new StateComponent(EntityState.GROWING));

        this.addComponent(new HealthComponent(300));
        this.addComponent(new ArmingComponent(3.0f));
        this.addComponent(new TeamComponent(Team.PLANT));
        this.addComponent(new GridCellComponent(col, row));

        this.addComponent(new ExplosiveComponent(1800, 150f, -1f));
    }
}
