package pvz.com.entities.projectiles;

import pvz.com.entities.Entity;
import pvz.com.entities.components.*;

public class PeaProjectile extends Entity {

    // startX, startY: Vị trí nòng súng của Peashooter
    // damage: Sát thương viên đạn (lấy từ Peashooter truyền vào)
    public PeaProjectile(float startX, float startY, int damage) {
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
