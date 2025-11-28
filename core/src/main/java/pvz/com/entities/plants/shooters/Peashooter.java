package pvz.com.entities.plants.shooters;

import pvz.com.entities.plants.Plant;
import pvz.com.entities.components.*;
import pvz.com.entities.projectiles.PeaProjectile;
import pvz.com.managers.GridConfig;

public class Peashooter extends Plant {

    private static final float SCALE_X = 0.7f; // rộng 70% ô
    private static final float SCALE_Y = 0.8f; // cao 80% ô

    public Peashooter(float x, float y) {
        super(
                x,
                y,
                GridConfig.CELL_WIDTH * SCALE_X,
                GridConfig.CELL_HEIGHT * SCALE_Y);

        this.addComponent(new SpriteComponent("assets/images/Plants/peashooterani.gif"));
        this.addComponent(new HealthComponent(100));
        this.addComponent(new PlantAttackComponent(
                20,
                300f,
                PeaProjectile.class,
                PlantDamageType.FIRE,
                1.5f));
    }
}
