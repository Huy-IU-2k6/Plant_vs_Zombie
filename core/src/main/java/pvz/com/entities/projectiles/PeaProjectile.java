package pvz.com.entities.projectiles;

import pvz.com.entities.Entity;
import pvz.com.entities.components.*;
import pvz.com.entities.components.DamageComponent;
import pvz.com.managers.DesignConfig;

public class PeaProjectile extends Entity {

    // ===== CONFIG =====
    private static final float PEA_SPEED = 150f;

    private static final float PEA_WIDTH = DesignConfig.PEA_WIDTH;
    private static final float PEA_HEIGHT = DesignConfig.PEA_HEIGHT;

    public PeaProjectile(float x, float y, int damage) {
        super();

        // 1. Vị trí
        this.addComponent(new PositionComponent(x, y));

        // 2. Movement: bay sang phải
        this.addComponent(new MovementComponent(PEA_SPEED, 0f));

        // 3. Sprite + scale size
        SpriteComponent sprite = new SpriteComponent("images/Plants/peabullet.png");
        sprite.sprite.setSize(PEA_WIDTH, PEA_HEIGHT);
        this.addComponent(sprite);

        // 4. Bounds & Size trùng kích thước
        this.addComponent(new BoundsComponent(x, y, PEA_WIDTH, PEA_HEIGHT));
        this.addComponent(new SizeComponent(PEA_WIDTH, PEA_HEIGHT));

        // 5. Sát thương của viên đạn
        this.addComponent(new DamageComponent(damage));

        // 6. Tag để ProjectileCollisionSystem nhận diện đây là đạn
        this.addComponent(new ProjectileTagComponent());
    }
}
