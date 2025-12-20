package pvz.com.entities.systems;

import java.util.List;
import pvz.com.entities.Entity;
import pvz.com.entities.components.state.EntityState;
import pvz.com.entities.components.state.StateComponent;
import pvz.com.entities.components.combat.ArmingComponent;

public class ArmingSystem {
    public void update(List<Entity> entities, float delta) {
        for (Entity entity : entities) {
            ArmingComponent arming = entity.getComponent(ArmingComponent.class);
            StateComponent state = entity.getComponent(StateComponent.class);

            if (arming == null || state == null)
                continue;

            if (!arming.isArmed) {
                if (state.get() == EntityState.UNARMED) {
                    arming.armingTimer -= delta;

                    if (arming.armingTimer <= 0) {
                        arming.isArmed = true;

                        state.set(EntityState.RISING);
                        state.timeInState = 0f;
                    }
                }
            }
        }
    }
}
