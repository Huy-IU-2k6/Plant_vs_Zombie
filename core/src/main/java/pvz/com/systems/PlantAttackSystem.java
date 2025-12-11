package pvz.com.systems;

import pvz.com.entities.plants.Plant;
import pvz.com.entities.components.PlantAttackComponent;
import pvz.com.entities.components.PositionComponent;
import pvz.com.entities.Zombies.Zombies; // Import Zombie base class
import pvz.com.logic.ZombieWaveController; // Import Controller

import java.util.List;

public class PlantAttackSystem {
    private IGameSpawner spawner;
    private ZombieWaveController zombieController; // Reference to check zombies

    // [FIX] Constructor now accepts ZombieWaveController
    public PlantAttackSystem(IGameSpawner spawner, ZombieWaveController zombieController) {
        this.spawner = spawner;
        this.zombieController = zombieController;
    }

    public void update(List<Plant> plants, float deltaTime) {
        for (Plant plant : plants) {
            PlantAttackComponent attacker = plant.getComponent(PlantAttackComponent.class);
            PositionComponent pos = plant.getComponent(PositionComponent.class);

            if (attacker != null && pos != null) {
                // Increase cooldown timer
                attacker.cooldown.timer += deltaTime;

                // If ready to shoot
                if (attacker.cooldown.timer >= attacker.cooldown.cooldownTime) {
                    
                    // Check if there is a zombie in range before shooting
                    if (shouldShoot(pos, attacker.range)) {
                        attacker.cooldown.timer = 0; // Reset cooldown

                        // Spawn projectile
                        spawner.spawnProjectile(
                                pos.x + 20, 
                                pos.y + 50, // Adjusted offset based on your previous code
                                attacker.damage,
                                attacker.damageType,
                                attacker.projectileType);
                    } else {
                        // Keep timer maxed so it shoots instantly when a zombie appears
                        attacker.cooldown.timer = attacker.cooldown.cooldownTime;
                    }
                }
            }
        }
    }

    // Logic to check if any zombie is in the lane and range
    private boolean shouldShoot(PositionComponent plantPos, float range) {
        if (zombieController == null) return false;

        for (Zombies z : zombieController.getZombies()) {
            // Ignore dead or dying zombies
            if (z.isDead() || z.getHealth() <= 0) continue;

            // 1. Check Lane (Y Check with tolerance)
            // Plants and zombies on the same row should have similar Y coordinates
            if (Math.abs(z.getY() - plantPos.y) > 50f) {
                continue; 
            }

            // 2. Check Range (X Check)
            // Zombie must be to the right of the plant AND within range
            if (z.getX() > plantPos.x && (z.getX() - plantPos.x) <= range) {
                return true; 
            }
        }
        
        return false; 
    }
}