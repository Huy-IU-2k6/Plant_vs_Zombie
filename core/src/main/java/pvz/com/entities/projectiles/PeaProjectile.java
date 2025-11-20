package pvz.com.entities.projectiles;

import pvz.com.entities.Entity;
import pvz.com.entities.components.*;

public class PeaProjectile extends Entity {
    public PeaProjectile(float x, float y, int damage) {
        super();
        // Đạn nhỏ hơn cây, ví dụ 20x20
        this.addComponent(new PositionComponent(x, y));
        this.addComponent(new SizeComponent(20, 20));
        this.addComponent(new BoundsComponent(x, y, 20, 20));
        this.addComponent(new SpriteComponent("assets/images/Plants/peabullet.png"));
        
        // Component di chuyển (để System xử lý bay)
        // this.addComponent(new MovementComponent(speedX, speedY...)); 
    }
}