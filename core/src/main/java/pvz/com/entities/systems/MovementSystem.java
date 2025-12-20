package pvz.com.entities.systems;

import java.util.List;
import pvz.com.entities.Entity;
import pvz.com.entities.components.physics.PositionComponent;
import pvz.com.entities.components.physics.MovementComponent;

public class MovementSystem {

    public void update(List<Entity> entities, float deltaTime) {
        for (Entity e : entities) {
            if (e.markedForRemoval)
                continue;

            PositionComponent pos = e.getComponent(PositionComponent.class);
            MovementComponent mov = e.getComponent(MovementComponent.class);

            if (pos != null && mov != null) {
                pos.x += mov.velocity.x * deltaTime;
                pos.y += mov.velocity.y * deltaTime;
            }
        }
    }
}
