package pvz.com.entities.suns;

import pvz.com.entities.Entity;
import pvz.com.entities.components.*;
import pvz.com.managers.DesignConfig;

public class Sun extends Entity {

    private static final float SUN_WIDTH = DesignConfig.SUN_WIDTH;
    private static final float SUN_HEIGHT = DesignConfig.SUN_HEIGHT;

    public Sun(float x, float y, int amount) {
        // 1. Vị trí
        this.addComponent(new PositionComponent(x, y));

        // 2. Sprite + chỉnh size
        SpriteComponent sprite = new SpriteComponent("images/items/Sun.png");
        sprite.sprite.setSize(SUN_WIDTH, SUN_HEIGHT);
        this.addComponent(sprite);

        // 3. Bounds & Size trùng kích thước
        this.addComponent(new SizeComponent(SUN_WIDTH, SUN_HEIGHT));
        this.addComponent(new BoundsComponent(x, y, SUN_WIDTH, SUN_HEIGHT));

        // 4. Component để nhặt (sống 7 giây)
        this.addComponent(new SunPickupComponent(amount, 7f));
    }
}
