// File: FrozenPeaProjectile.java
package pvz.com.entities.projectiles;

import pvz.com.entities.Entity;
import pvz.com.entities.components.*;

public class FrozenPeaProjectile extends Entity {
    public FrozenPeaProjectile(float x, float y, int damage) {
        super();
        this.addComponent(new PositionComponent(x, y));
        this.addComponent(new MovementComponent(800f, 0f)); // Tốc độ bay
        this.addComponent(new SpriteComponent("images/Plants/SnowPea.png")); // Ảnh đạn xanh
        this.addComponent(new BoundsComponent(x, y, 20, 20));
        this.addComponent(new SizeComponent(20, 20));
        this.addComponent(new DamageComponent(damage));
        this.addComponent(new ProjectileTagComponent());

       
    }
}
