package pvz.com.systems;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import pvz.com.entities.Entity;
import pvz.com.entities.Zombies.Zombies;
import pvz.com.entities.components.*;
import pvz.com.entities.plants.Plant;
import pvz.com.entities.projectiles.FrozenPeaProjectile; // [FIX] Import specific projectiles
import pvz.com.entities.projectiles.PeaProjectile;
import pvz.com.logic.PlantGridController;
import pvz.com.logic.ZombieWaveController;
import pvz.com.managers.GridConfig;

public class CollisionSystem {

    private final List<Entity> entities;
    private final ZombieWaveController zombieWaveController;
    private final PlantGridController plantGridController;

    private final Map<Zombies, Plant> eatingTargets = new HashMap<>();

    public CollisionSystem(List<Entity> entities,
            ZombieWaveController zombieWaveController,
            PlantGridController plantGridController) {
        this.entities = entities;
        this.zombieWaveController = zombieWaveController;
        this.plantGridController = plantGridController;
    }

    public void update(float deltaTime) {
        Plant[][] grid = plantGridController.getPlantGrid();

        for (Zombies zombie : zombieWaveController.getZombies()) {
            if (zombie.isDead()) {
                zombie.setEating(false);
                eatingTargets.remove(zombie);
                continue;
            }

            Rectangle zRect = zombie.getBounds();
            
            // [FIX] Correctly calculate row based on GridConfig
            int zombieRow = getRowForZombie(zRect);

            // 1) ZOMBIE vs PROJECTILE
            handleZombieProjectiles(zombie, zRect);

            // 2) ZOMBIE vs PLANT
            handleZombiePlants(zombie, zombieRow, grid, zRect, deltaTime);
        }
    }

    // =========================
    // ZOMBIE vs BULLET
    // =========================
    private void handleZombieProjectiles(Zombies zombie, Rectangle zRect) {
        for (Entity projectile : entities) {
            if (projectile.markedForRemoval) continue;

            // [FIX] Check if entity is a Projectile using instance check or checking components
            // Since we don't have ProjectileTagComponent, we check class type
            boolean isProjectile = (projectile instanceof PeaProjectile) || (projectile instanceof FrozenPeaProjectile);
            if (!isProjectile) continue;

            // Get Bounds & Damage from Components (assuming projectiles have these)
            // If projectiles are Entities, they must have components added in their constructor!
            // BUT: In your GameWorld, you created PeaProjectile as 'Entity' subclass.
            // Let's assume you added BoundsComponent and a custom 'DamageComponent' or similar.
            
            // SIMPLIFIED LOGIC for compatibility:
            // Check position intersection manually if components are missing
            PositionComponent pPos = projectile.getComponent(PositionComponent.class);
            if (pPos == null) continue;
            
            Rectangle pRect = new Rectangle(pPos.x, pPos.y, 20, 20); // Approx size

            if (pRect.overlaps(zRect)) {
                // Apply Damage
                // Default damage is 20
                zombie.takeDamage(20); 
                
                // Apply Slow if Frozen
                if (projectile instanceof FrozenPeaProjectile) {
                    zombie.applySlow(5f, 0.5f); // Slow for 5s, 50% speed
                }

                // Remove Bullet
                projectile.markedForRemoval = true;
            }
        }
    }

    // =========================
    // ZOMBIE vs PLANT
    // =========================
    private void handleZombiePlants(Zombies zombie,
            int zombieRow,
            Plant[][] grid,
            Rectangle zRect,
            float deltaTime) {

        // Check if row is valid
        if (zombieRow < 0 || zombieRow >= GridConfig.ROWS) {
            zombie.setEating(false);
            return;
        }

        Plant currentTarget = eatingTargets.get(zombie);

        // --- 1. EXISTING TARGET ---
        if (currentTarget != null) {
            HealthComponent hp = currentTarget.getComponent(HealthComponent.class);
            
            if (currentTarget.markedForRemoval || hp == null || hp.currentHealth <= 0) {
                eatingTargets.remove(zombie);
                zombie.setEating(false);
                return;
            }
            
            // Continue eating
            zombie.setEating(true);
            damagePlantAndMaybeRemove(currentTarget, deltaTime);
            return;
        }

        // --- 2. FIND NEW TARGET ---
        // Look through plants in the same row
        for (int col = 0; col < GridConfig.COLS; col++) {
            Plant plant = grid[zombieRow][col];
            if (plant == null || plant.markedForRemoval) continue;

            // Check collision using Position/Grid info
            // Plant bounds (approximate from grid)
            float pX = GridConfig.getCellOriginX(col);
            float pY = GridConfig.getCellOriginY(zombieRow);
            Rectangle pRect = new Rectangle(pX, pY, GridConfig.CELL_WIDTH, GridConfig.CELL_HEIGHT);

            if (zRect.overlaps(pRect)) {
                // [FIX] Potato Mine Logic (using ExplosiveComponent)
                ExplosiveComponent explosive = plant.getComponent(ExplosiveComponent.class);
                
                // If it's a Potato Mine (has explosion) and armed logic (simplified)
                // For now, treat Potato Mine as instant explode on contact
                if (explosive != null && plant.getClass().getSimpleName().equals("PotatoMine")) {
                     explosive.fuseTime = 0; // Explode immediately
                     return; // Don't eat it
                }

                // Normal Plant -> Eat it
                eatingTargets.put(zombie, plant);
                zombie.setEating(true);
                return;
            }
        }

        zombie.setEating(false);
    }

    private void damagePlantAndMaybeRemove(Plant plant, float deltaTime) {
        HealthComponent health = plant.getComponent(HealthComponent.class);
        if (health == null) return;

        float damagePerSecond = 50f; // Adjust eating speed
        health.currentHealth -= damagePerSecond * deltaTime;

        if (health.currentHealth <= 0) {
            plant.markedForRemoval = true;
            
            // Remove from grid
            GridPositionComponent gridPos = plant.getComponent(GridPositionComponent.class);
            if (gridPos != null) {
                plantGridController.unregisterPlantAtCell(gridPos.row, gridPos.col);
            }
        }
    }

    private int getRowForZombie(Rectangle zRect) {
        float centerY = zRect.y + zRect.height / 2f;
        // Use GridConfig to convert World Y to Row Index
        int row = (int) ((centerY - GridConfig.START_Y) / GridConfig.CELL_HEIGHT);
        return row;
    }
}