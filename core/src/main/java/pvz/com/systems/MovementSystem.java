package pvz.com.systems;

import java.util.List;
import pvz.com.entities.Entity;
import pvz.com.entities.components.PositionComponent;
import pvz.com.entities.components.MovementComponent;

public class MovementSystem {

    public void update(List<Entity> entities, float deltaTime) {
        for (Entity e : entities) {
            PositionComponent pos = e.getComponent(PositionComponent.class);
            MovementComponent mov = e.getComponent(MovementComponent.class);

            if (pos != null && mov != null) {
                pos.x += mov.velocity.x * deltaTime;
                pos.y += mov.velocity.y * deltaTime;
            }
        }
    }
}
