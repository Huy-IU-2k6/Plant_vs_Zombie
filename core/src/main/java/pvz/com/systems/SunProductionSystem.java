package pvz.com.systems;

import pvz.com.entities.plants.Plant; // Giả sử Plant là class cha chứa các component
import pvz.com.entities.components.SunProducerComponent;
import pvz.com.entities.components.PositionComponent;
import java.util.List;

public class SunProductionSystem {
    private IGameSpawner spawner;

    public SunProductionSystem(IGameSpawner spawner) {
        this.spawner = spawner;
    }

    public void update(List<Plant> plants, float deltaTime) {
        for (Plant plant : plants) {
            // Kiểm tra xem cây này có khả năng sinh Sun không
            SunProducerComponent producer = plant.getComponent(SunProducerComponent.class);
            PositionComponent pos = plant.getComponent(PositionComponent.class);

            if (producer != null && pos != null) {
                // Logic tính thời gian
                producer.cooldown.timer += deltaTime;
                
                if (producer.cooldown.timer >= producer.cooldown.cooldownTime) {
                    producer.cooldown.timer = 0; // Reset
                    
                    // Gọi qua Interface (DIP)
                    spawner.spawnSun(pos.x, pos.y, producer.sunAmount);
                }
            }
        }
    }
}