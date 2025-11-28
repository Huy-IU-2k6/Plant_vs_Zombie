package pvz.com.systems;

import pvz.com.entities.Entity;
import pvz.com.entities.components.PositionComponent; // Import thêm cái này
import pvz.com.entities.components.SunProducerComponent;
import java.util.List;

public class SunProductionSystem {
    private IGameSpawner spawner;
    private List<Entity> entities;

    public SunProductionSystem(IGameSpawner spawner, List<Entity> entities) {
        this.spawner = spawner;
        this.entities = entities;
    }

    public void update(float deltaTime) {
        for (Entity entity : entities) {
            // Kiểm tra xem entity có phải là máy tạo Sun không
            if (entity.hasComponent(SunProducerComponent.class)) {
                SunProducerComponent sunProd = entity.getComponent(SunProducerComponent.class);

                // Cộng dồn thời gian
                sunProd.cooldown.timer += deltaTime;

                // Nếu đủ thời gian cooldown -> Đẻ Sun
                if (sunProd.cooldown.timer >= sunProd.cooldown.cooldownTime) {
                    sunProd.cooldown.timer = 0;

                    // --- LOGIC MỚI: LẤY VỊ TRÍ THỰC TẾ ---
                    float spawnX = 0;
                    float spawnY = 0;

                    // Kiểm tra xem cây này có vị trí không (chắc chắn là có, nhưng check cho an
                    // toàn)
                    if (entity.hasComponent(PositionComponent.class)) {
                        PositionComponent pos = entity.getComponent(PositionComponent.class);
                        // Cộng thêm một chút offset (ví dụ +0, +20) để Sun hiện ra ở giữa hoặc trên đầu
                        // cây
                        spawnX = pos.x + 10f;
                        spawnY = pos.y + 10f;
                    }

                    // Gọi Spawner tạo Sun tại vị trí vừa lấy được
                    spawner.spawnSun(spawnX, spawnY, sunProd.sunAmount);
                }
            }
        }
    }
}
