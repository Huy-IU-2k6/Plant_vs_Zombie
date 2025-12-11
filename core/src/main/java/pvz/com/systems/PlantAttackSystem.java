package pvz.com.systems;

import pvz.com.entities.plants.Plant;
import pvz.com.entities.components.PlantAttackComponent;
import pvz.com.entities.components.PositionComponent;
import pvz.com.entities.components.HealthComponent;
import pvz.com.entities.Zombies.Zombies; 
import pvz.com.logic.ZombieWaveController; 
import pvz.com.managers.DesignConfig; // [1] Import để lấy chiều rộng màn hình

import java.util.List;

public class PlantAttackSystem {
    private IGameSpawner spawner;
    private ZombieWaveController zombieController; 

    public PlantAttackSystem(IGameSpawner spawner, ZombieWaveController zombieController) {
        this.spawner = spawner;
        this.zombieController = zombieController;
    }

    public void update(List<Plant> plants, float deltaTime) {
        for (Plant plant : plants) {
            HealthComponent health = plant.getComponent(HealthComponent.class);
            if (health != null && health.currentHealth <= 0) {
                continue;
            }

            PlantAttackComponent attacker = plant.getComponent(PlantAttackComponent.class);
            PositionComponent pos = plant.getComponent(PositionComponent.class);

            if (attacker != null && pos != null) {
                attacker.cooldown.timer += deltaTime;

                if (attacker.cooldown.timer >= attacker.cooldown.cooldownTime) {
                    
                    if (shouldShoot(pos, attacker.range)) {
                        attacker.cooldown.timer = 0; 

                        spawner.spawnProjectile(
                                pos.x + 20, 
                                pos.y + 50, 
                                attacker.damage,
                                attacker.damageType,
                                attacker.projectileType);
                    } else {
                        attacker.cooldown.timer = attacker.cooldown.cooldownTime;
                    }
                }
            }
        }
    }

    private boolean shouldShoot(PositionComponent plantPos, float range) {
        if (zombieController == null) return false;

        // [2] Lấy chiều rộng màn hình (800f)
        float screenRightEdge = DesignConfig.BASE_SCREEN_W; 

        for (Zombies z : zombieController.getZombies()) {
            if (z.isDead() || z.getHealth() <= 0) continue;

            // [FIX QUAN TRỌNG] 
            // Nếu Zombie chưa bước vào màn hình (X > 800), thì KỆ NÓ (không bắn)
            if (z.getX() > (screenRightEdge-100f)) {
                continue;
            }

            // 1. Check Lane
            if (Math.abs(z.getY() - plantPos.y) > 50f) {
                continue; 
            }

            // 2. Check Range
            if (z.getX() > plantPos.x && (z.getX() - plantPos.x) <= range) {
                return true; 
            }
        }
        
        return false; 
    }
}