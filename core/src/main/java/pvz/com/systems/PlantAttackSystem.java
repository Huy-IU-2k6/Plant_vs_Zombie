package pvz.com.systems;

import pvz.com.entities.plants.Plant;
import pvz.com.entities.components.PlantAttackComponent;
import pvz.com.entities.components.PositionComponent;
import java.util.List;

public class PlantAttackSystem {
    private IGameSpawner spawner;

    public PlantAttackSystem(IGameSpawner spawner) {
        this.spawner = spawner;
    }

    public void update(List<Plant> plants, float deltaTime) {
        for (Plant plant : plants) {
            PlantAttackComponent attacker = plant.getComponent(PlantAttackComponent.class);
            PositionComponent pos = plant.getComponent(PositionComponent.class);

            if (attacker != null && pos != null) {
                attacker.cooldown.timer += deltaTime;

                // Trong thực tế, bạn nên thêm logic check: "Có Zombie ở cùng hàng không?" tại
                // đây
                // if (isZombieInLane(plant, attacker.range)) { ... }

                if (attacker.cooldown.timer >= attacker.cooldown.cooldownTime) {
                    attacker.cooldown.timer = 0;

                    // Bắn đạn dựa trên thông số trong Component
                    spawner.spawnProjectile(
                            pos.x + 20, // Offset để đạn bay ra từ miệng cây
                            pos.y + 50,
                            attacker.damage,
                            attacker.damageType,
                            attacker.projectileType);
                }
            }
        }
    }
}
