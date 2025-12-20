package pvz.com.entities.plants;

import pvz.com.entities.Entity;
import pvz.com.entities.components.*;
import pvz.com.entities.components.PlantTypeComponent;

public class Plant extends Entity {


    public Plant(float x, float y, float width, float height) {
        super();
        this.addComponent(new PositionComponent(x, y));
        this.addComponent(new SizeComponent(width, height));
        this.addComponent(new BoundsComponent(x, y, width, height));
    }


    public Plant(PlantType type, float x, float y, float width, float height) {
        this(x, y, width, height);
        this.addComponent(new PlantTypeComponent(type));
    }
}
