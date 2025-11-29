package pvz.com.entities.projectiles;

import pvz.com.entities.Entity;
import pvz.com.entities.components.*;
import pvz.com.entities.components.DamageComponent;

public class PeaProjectile extends Entity {
    public PeaProjectile(float x, float y, int damage) {
        super();

        this.addComponent(new PositionComponent(x, y));
        this.addComponent(new MovementComponent(200f, 0f));
        this.addComponent(new SpriteComponent("assets/images/Plants/peabullet.png"));
        this.addComponent(new BoundsComponent(x, y, 20, 20));
        this.addComponent(new SizeComponent(20, 20));

        // QUAN TRỌNG: sát thương của viên đạn
        this.addComponent(new DamageComponent(damage));

        // Tag để ProjectileCollisionSystem nhận diện đây là đạn
        this.addComponent(new ProjectileTagComponent());
    }
}
