package pvz.com.systems;

import pvz.com.entities.Entity;
import pvz.com.entities.components.EntityState;
import pvz.com.entities.components.HealthComponent;
import pvz.com.entities.components.StateComponent;
import pvz.com.entities.plants.defenders.Wallnut;

import java.util.List;

public class WallnutStateSystem {

    private static final float THRESHOLD_CRACKED_1 = 0.75f;
    private static final float THRESHOLD_CRACKED_2 = 0.25f;

    public void update(List<Entity> entities) {
        for (Entity entity : entities) {
            if (!(entity instanceof Wallnut)) {
                continue;
            }

            HealthComponent health = entity.getComponent(HealthComponent.class);
            StateComponent state = entity.getComponent(StateComponent.class);

            if (health == null || state == null) {
                continue;
            }

            float healthPercent = (float) health.currentHealth / health.maxHealth;

            EntityState newState;
            if (healthPercent > THRESHOLD_CRACKED_1) {
                newState = EntityState.WALLNUT_FULL;
            } else if (healthPercent > THRESHOLD_CRACKED_2) {
                newState = EntityState.WALLNUT_CRACKED_1;
            } else {
                newState = EntityState.WALLNUT_CRACKED_2;
            }

            if (state.get() != newState) {
                state.set(newState);
            }
        }
    }
}
