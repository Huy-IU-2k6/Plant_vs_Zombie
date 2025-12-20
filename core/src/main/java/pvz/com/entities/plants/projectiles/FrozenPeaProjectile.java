package pvz.com.entities.plants.projectiles;

import pvz.com.entities.Entity;
import pvz.com.entities.components.tags.ProjectileTagComponent;
import pvz.com.entities.components.render.SpriteComponent;
import pvz.com.entities.components.physics.PositionComponent;
import pvz.com.entities.components.physics.MovementComponent;
import pvz.com.entities.components.physics.BoundsComponent;
import pvz.com.entities.components.physics.SizeComponent;
import pvz.com.entities.components.combat.DamageComponent;

public class FrozenPeaProjectile extends Entity {
    public FrozenPeaProjectile(float x, float y, int damage) {
        super();
        this.addComponent(new PositionComponent(x, y));
        this.addComponent(new MovementComponent(800f, 0f));
        // TODO: fix freeze feature
        this.addComponent(new SpriteComponent("images/items/snow_bullet_item.png"));
        this.addComponent(new BoundsComponent(x, y, 20, 20));
        this.addComponent(new SizeComponent(20, 20));
        this.addComponent(new DamageComponent(damage));
        this.addComponent(new ProjectileTagComponent());

    }
}
