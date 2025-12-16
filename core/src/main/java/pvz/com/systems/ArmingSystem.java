package pvz.com.systems;

import java.util.List;
import pvz.com.entities.Entity;
import pvz.com.entities.components.*;

public class ArmingSystem {
    public void update(List<Entity> entities, float delta) {
        for (Entity entity : entities) {
            ArmingComponent arming = entity.getComponent(ArmingComponent.class);
            StateComponent state = entity.getComponent(StateComponent.class);

            if (arming == null || state == null) continue;

            // Nếu chưa vũ trang (đang là cục đất)
            if (!arming.isArmed) {
                arming.armingTimer -= delta;

                if (arming.armingTimer <= 0) {
                    // => ĐÃ CHÍN: Chuyển sang trạng thái sẵn sàng
                    arming.isArmed = true;
                    
                    // Đổi trạng thái animation sang IDLE (Cây mọc lên)
                    state.set(EntityState.IDLE);
                    state.timeInState = 0f; // Reset thời gian animation
                }
            }
        }
    }
}