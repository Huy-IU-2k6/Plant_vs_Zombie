package pvz.com.entities.plants.projectiles;

import pvz.com.entities.Entity;
import pvz.com.managers.DesignConfig;
import pvz.com.entities.components.tags.ProjectileTagComponent;
import pvz.com.entities.components.render.SpriteComponent;
import pvz.com.entities.components.physics.PositionComponent;
import pvz.com.entities.components.physics.MovementComponent;
import pvz.com.entities.components.physics.BoundsComponent;
import pvz.com.entities.components.physics.SizeComponent;
import pvz.com.entities.components.combat.DamageComponent;

public class PeaProjectile extends Entity {

    private static final float PEA_SPEED = 800f;

    private static final float PEA_WIDTH = DesignConfig.PEA_WIDTH;
    private static final float PEA_HEIGHT = DesignConfig.PEA_HEIGHT;

    public PeaProjectile(float x, float y, int damage) {
        super();

        this.addComponent(new PositionComponent(x, y));

        this.addComponent(new MovementComponent(PEA_SPEED, 0f));

        SpriteComponent sprite = new SpriteComponent("images/items/normal_bullet_item.png");
        sprite.sprite.setSize(PEA_WIDTH, PEA_HEIGHT);
        this.addComponent(sprite);

        this.addComponent(new BoundsComponent(x, y, PEA_WIDTH, PEA_HEIGHT));
        this.addComponent(new SizeComponent(PEA_WIDTH, PEA_HEIGHT));

        this.addComponent(new DamageComponent(damage));

        this.addComponent(new ProjectileTagComponent());
    }
}
