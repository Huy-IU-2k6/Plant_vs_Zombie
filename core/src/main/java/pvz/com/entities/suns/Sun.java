package pvz.com.entities.suns;

import pvz.com.entities.Entity;
import pvz.com.entities.components.*;

public class Sun extends Entity {

    private static final float SUN_WIDTH = 40f;
    private static final float SUN_HEIGHT = 40f;

    public Sun(float x, float y, int amount) {
        // 1. Vị trí
        this.addComponent(new PositionComponent(x, y));

        // 2. Sprite + chỉnh size nhỏ lại
        SpriteComponent sprite = new SpriteComponent("images/items/Sun.png");
        sprite.sprite.setSize(SUN_WIDTH, SUN_HEIGHT); // QUAN TRỌNG
        this.addComponent(sprite);

        // 3. Bounds & Size trùng kích thước
        this.addComponent(new SizeComponent(SUN_WIDTH, SUN_HEIGHT));
        this.addComponent(new BoundsComponent(x, y, SUN_WIDTH, SUN_HEIGHT));

        // 4. Component để nhặt
        this.addComponent(new SunPickupComponent(amount, 7f)); // sống 7 giây
    }
}
