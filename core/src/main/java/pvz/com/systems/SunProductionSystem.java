package pvz.com.systems;

import pvz.com.entities.Entity;
import pvz.com.entities.components.PositionComponent; // Import thêm cái này
import pvz.com.entities.components.SunProducerComponent;

import java.util.ArrayList;
import java.util.List;

public class SunProductionSystem {
    private IGameSpawner spawner;
    private List<Entity> entities;

    public SunProductionSystem(IGameSpawner spawner, List<Entity> entities) {
        this.spawner = spawner;
        this.entities = entities;
    }

    // struct nhỏ để lưu request spawn Sun
    private static class SunSpawnRequest {
        float x, y;
        int amount;

        SunSpawnRequest(float x, float y, int amount) {
            this.x = x;
            this.y = y;
            this.amount = amount;
        }
    }

    public void update(float deltaTime) {
        // DANH SÁCH SUN SẼ SPAWN SAU KHI DUYỆT XONG
        List<SunSpawnRequest> spawnRequests = new ArrayList<>();

        // Duyệt trên snapshot để tránh bị đổi list giữa chừng
        for (Entity entity : new ArrayList<>(entities)) {
            // Kiểm tra xem entity có phải là máy tạo Sun không
            if (entity.hasComponent(SunProducerComponent.class)) {
                SunProducerComponent sunProd = entity.getComponent(SunProducerComponent.class);

                // Cộng dồn thời gian
                sunProd.cooldown.timer += deltaTime;

                // Nếu đủ thời gian cooldown -> chuẩn bị đẻ Sun
                if (sunProd.cooldown.timer >= sunProd.cooldown.cooldownTime) {
                    sunProd.cooldown.timer = 0;

                    // --- LẤY VỊ TRÍ THỰC TẾ ---
                    float spawnX = 0;
                    float spawnY = 0;

                    if (entity.hasComponent(PositionComponent.class)) {
                        PositionComponent pos = entity.getComponent(PositionComponent.class);
                        // offset nhẹ cho đẹp
                        spawnX = pos.x + 10f;
                        spawnY = pos.y + 10f;
                    }

                    // KHÔNG spawn ngay lập tức, chỉ lưu request
                    spawnRequests.add(new SunSpawnRequest(spawnX, spawnY, sunProd.sunAmount));
                }
            }
        }

        // SAU KHI DUYỆT XONG HẾT MỚI THỰC SỰ SPAWN SUN
        for (SunSpawnRequest req : spawnRequests) {
            spawner.spawnSun(req.x, req.y, req.amount);
        }
    }
}
