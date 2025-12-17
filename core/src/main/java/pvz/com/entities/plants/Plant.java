package pvz.com.entities.plants;

import pvz.com.entities.Entity;
import pvz.com.entities.components.*;
import pvz.com.entities.components.PlantTypeComponent;

public class Plant extends Entity {

    // Giữ lại constructor cũ (để không vỡ code đang dùng)
    public Plant(float x, float y, float width, float height) {
        super();
        this.addComponent(new PositionComponent(x, y));
        this.addComponent(new SizeComponent(width, height));
        this.addComponent(new BoundsComponent(x, y, width, height));
    }

    // Constructor mới có type để shovel refund theo catalog
    public Plant(PlantType type, float x, float y, float width, float height) {
        this(x, y, width, height);
        this.addComponent(new PlantTypeComponent(type));
    }
}
