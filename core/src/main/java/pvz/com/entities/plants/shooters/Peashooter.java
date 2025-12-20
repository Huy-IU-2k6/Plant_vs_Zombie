package pvz.com.entities.plants.shooters;

import pvz.com.entities.plants.Plant;
import pvz.com.entities.plants.projectiles.PeaProjectile;
import pvz.com.managers.GridConfig;
import pvz.com.entities.factories.PlantAssetLoader;
import pvz.com.entities.components.state.EntityState;
import pvz.com.entities.components.state.StateComponent;
import pvz.com.entities.components.animation.AnimationComponent;
import pvz.com.entities.components.team.Team;
import pvz.com.entities.components.team.TeamComponent;
import pvz.com.entities.components.types.PlantDamageType;
import pvz.com.entities.components.grid.GridCellComponent;
import pvz.com.entities.components.render.SpriteComponent;
import pvz.com.entities.components.combat.HealthComponent;
import pvz.com.entities.components.combat.PlantAttackComponent;

public class Peashooter extends Plant {

    private static final float SCALE_X = 0.7f;
    private static final float SCALE_Y = 0.8f;

    public Peashooter(float x, float y, int col, int row) {
        super(
                x,
                y,
                GridConfig.CELL_WIDTH * SCALE_X,
                GridConfig.CELL_HEIGHT * SCALE_Y);

        var idleAnim = PlantAssetLoader.PEASHOOTER_IDLE;

        if (idleAnim == null) {
            System.err.println("Error: PEASHOOTER_IDLE is null. Call loadAll() first!");
            return;
        }

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
                1.5f));
    }

    public Peashooter(float x, float y) {
        this(x, y, -1, -1);
    }
}
