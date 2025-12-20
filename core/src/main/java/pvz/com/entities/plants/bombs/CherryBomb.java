package pvz.com.entities.plants.bombs;

import pvz.com.entities.plants.Plant;
import pvz.com.entities.factories.PlantAssetLoader;
import pvz.com.entities.components.state.EntityState;
import pvz.com.entities.components.state.StateComponent;
import pvz.com.entities.components.animation.AnimationComponent;
import pvz.com.entities.components.team.Team;
import pvz.com.entities.components.team.TeamComponent;
import pvz.com.entities.components.grid.GridCellComponent;
import pvz.com.entities.components.render.SpriteComponent;
import pvz.com.entities.components.combat.HealthComponent;
import pvz.com.entities.components.combat.ExplosiveComponent;

public class CherryBomb extends Plant {

    public CherryBomb(float x, float y, int col, int row) {

        super(x, y, 90, 90);

        var idleAnim = PlantAssetLoader.CHERRY_IDLE;
        var explodeAnim = PlantAssetLoader.CHERRY_EXPLODE;

        if (idleAnim == null) {
            System.err.println("Error: CHERRY_BOMB animations are null.");
            return;
        }

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
