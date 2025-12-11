package pvz.com.systems;

import pvz.com.entities.Entity;
import pvz.com.entities.components.EntityState;
import pvz.com.entities.components.HealthComponent;
import pvz.com.entities.components.StateComponent;
import pvz.com.entities.plants.defenders.Wallnut;

import java.util.List;

public class WallnutStateSystem {

    // Ngưỡng chuyển trạng thái
    private static final float THRESHOLD_CRACKED_1 = 0.75f; // Dưới 75% máu
    private static final float THRESHOLD_CRACKED_2 = 0.25f; // Dưới 25% máu

    public void update(List<Entity> entities) {
        for (Entity entity : entities) {
            // 1. Chỉ xử lý Wallnut
            if (!(entity instanceof Wallnut)) {
                continue;
            }

            // 2. Lấy component Máu và Trạng thái
            HealthComponent health = entity.getComponent(HealthComponent.class);
            StateComponent state = entity.getComponent(StateComponent.class);

            if (health == null || state == null) {
                continue;
            }

            // 3. Tính phần trăm máu còn lại
            float healthPercent = (float) health.currentHealth / health.maxHealth;

            // 4. Quyết định trạng thái dựa trên % máu
            EntityState newState;
            if (healthPercent > THRESHOLD_CRACKED_1) {
                newState = EntityState.WALLNUT_FULL;
            } else if (healthPercent > THRESHOLD_CRACKED_2) {
                newState = EntityState.WALLNUT_CRACKED_1;
            } else {
                newState = EntityState.WALLNUT_CRACKED_2;
            }

            // 5. Cập nhật trạng thái nếu có thay đổi
            // (Việc gán lại này sẽ khiến AnimationSystem tự động đổi animation vào frame tiếp theo)
            if (state.get() != newState) {
                state.set(newState);
            }
        }
    }
}