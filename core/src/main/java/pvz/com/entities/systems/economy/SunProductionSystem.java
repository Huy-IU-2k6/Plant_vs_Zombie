package pvz.com.entities.systems.economy;

import pvz.com.entities.Entity;
import pvz.com.entities.components.physics.PositionComponent;
import pvz.com.entities.components.economy.SunProducerComponent;
import pvz.com.entities.systems.interfaces.IGameSpawner;

import java.util.ArrayList;
import java.util.List;

public class SunProductionSystem {
    private IGameSpawner spawner;
    private List<Entity> entities;

    public SunProductionSystem(IGameSpawner spawner, List<Entity> entities) {
        this.spawner = spawner;
        this.entities = entities;
    }

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
        List<SunSpawnRequest> spawnRequests = new ArrayList<>();

        for (Entity entity : new ArrayList<>(entities)) {
            if (entity.hasComponent(SunProducerComponent.class)) {
                SunProducerComponent sunProd = entity.getComponent(SunProducerComponent.class);

                sunProd.cooldown.timer += deltaTime;

                if (sunProd.cooldown.timer >= sunProd.cooldown.cooldownTime) {
                    sunProd.cooldown.timer = 0;

                    float spawnX = 0;
                    float spawnY = 0;

                    if (entity.hasComponent(PositionComponent.class)) {
                        PositionComponent pos = entity.getComponent(PositionComponent.class);
                        spawnX = pos.x + 10f;
                        spawnY = pos.y + 10f;
                    }

                    spawnRequests.add(new SunSpawnRequest(spawnX, spawnY, sunProd.sunAmount));
                }
            }
        }

        for (SunSpawnRequest req : spawnRequests) {
            spawner.spawnSun(req.x, req.y, req.amount);
        }
    }
}
