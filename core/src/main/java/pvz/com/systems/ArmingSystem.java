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

            // Nếu chưa vũ trang (đang là cục đất/nhấp nháy)
            if (!arming.isArmed) {
                // Lưu ý: Chỉ trừ thời gian khi đang ở trạng thái UNARMED (nhấp nháy đèn)
                // Tránh việc nó đếm giờ ngay cả khi đang GROWING
                if (state.get() == EntityState.UNARMED) {
                    arming.armingTimer -= delta;

                    if (arming.armingTimer <= 0) {
                        // => ĐÃ CHÍN
                        arming.isArmed = true;
                        
                        // [THAY ĐỔI] Chuyển sang trạng thái RISING (Trồi lên)
                        state.set(EntityState.RISING);
                        state.timeInState = 0f; 
                    }
                }
            }
        }
    }
}