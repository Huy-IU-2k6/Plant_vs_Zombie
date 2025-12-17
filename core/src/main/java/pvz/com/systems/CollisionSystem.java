package pvz.com.systems;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import pvz.com.entities.Entity;
import pvz.com.entities.Zombies.Zombies;
import pvz.com.entities.components.ArmingComponent;
import pvz.com.entities.components.BoundsComponent;
import pvz.com.entities.components.DamageComponent;
import pvz.com.entities.components.ExplosiveComponent;
import pvz.com.entities.components.GridCellComponent;
import pvz.com.entities.components.HealthComponent;
import pvz.com.entities.components.PositionComponent;
import pvz.com.entities.components.ProjectileTagComponent;
import pvz.com.entities.components.Team;
import pvz.com.entities.components.TeamComponent;
import pvz.com.entities.plants.Plant;
import pvz.com.logic.GameState;
import pvz.com.logic.PlantGridController;
import pvz.com.logic.ZombieWaveController;
import pvz.com.managers.GridConfig;

public class CollisionSystem {

    private final List<Entity> entities;
    private final ZombieWaveController zombieWaveController;
    private final PlantGridController plantGridController;

    // [MỚI] để set game over
    private final GameState gameState;

    private final Map<Zombies, Plant> eatingTargets = new HashMap<>();

    public CollisionSystem(List<Entity> entities,
            ZombieWaveController zombieWaveController,
            PlantGridController plantGridController,
            GameState gameState) {
        this.entities = entities;
        this.zombieWaveController = zombieWaveController;
        this.plantGridController = plantGridController;
        this.gameState = gameState;
    }

    public void update(float deltaTime) {
        if (gameState != null && gameState.isGameOver())
            return;

        Plant[][] grid = plantGridController.getPlantGrid();

        // “cổng thua”: zombie qua mép trái của cột 0 một chút
        float loseX = GridConfig.getCellOriginX(0) - GridConfig.CELL_WIDTH * 0.35f;

        for (Zombies zombie : zombieWaveController.getZombies()) {
            if (zombie == null)
                continue;

            if (zombie.isDead()) {
                zombie.setEating(false);
                eatingTargets.remove(zombie);
                continue;
            }

            Rectangle zRect = zombie.getBounds();

            // =============================
            // [MỚI] THUA: zombie tới cổng trái
            // =============================
            if (zRect.x <= loseX) {
                if (gameState != null)
                    gameState.setGameOver(false);
                // dọn trạng thái ăn cho sạch
                eatingTargets.clear();
                zombie.setEating(false);
                return; // dừng update ngay
            }

            int zombieRow = getRowForZombie(zRect);

            handleZombieProjectiles(zombie, zRect);
            handleZombiePlants(zombie, zombieRow, grid, zRect, deltaTime);
        }
    }

    private void handleZombieProjectiles(Zombies zombie, Rectangle zRect) {
        for (Entity projectile : entities) {
            if (projectile == null || projectile.markedForRemoval)
                continue;
            if (projectile.getComponent(ProjectileTagComponent.class) == null)
                continue;

            BoundsComponent pBounds = projectile.getComponent(BoundsComponent.class);
            DamageComponent damage = projectile.getComponent(DamageComponent.class);
            if (pBounds == null || damage == null)
                continue;

            syncBoundsWithPosition(projectile);

            if (!pBounds.bounds.overlaps(zRect))
                continue;

            zombie.takeDamage(damage.amount);
            projectile.markedForRemoval = true;
        }
    }

    private void handleZombiePlants(Zombies zombie,
            int zombieRow,
            Plant[][] grid,
            Rectangle zRect,
            float deltaTime) {

        if (!GridConfig.isInsideGrid(zombieRow, 0)) {
            eatingTargets.remove(zombie);
            zombie.setEating(false);
            return;
        }

        Plant currentTarget = eatingTargets.get(zombie);

        if (currentTarget != null) {
            if (currentTarget.markedForRemoval || !currentTarget.hasComponent(HealthComponent.class)) {
                eatingTargets.remove(zombie);
                zombie.setEating(false);
                return;
            }

            BoundsComponent pb = currentTarget.getComponent(BoundsComponent.class);
            if (pb == null || !Intersector.overlaps(zRect, pb.bounds)) {
                eatingTargets.remove(zombie);
                zombie.setEating(false);
                return;
            }

            zombie.setEating(true);
            damagePlantAndMaybeRemove(currentTarget, deltaTime);
            return;
        }

        Plant[] rowPlants = grid[zombieRow];
        for (Plant plant : rowPlants) {
            if (plant == null || plant.markedForRemoval)
                continue;
            if (!plant.hasComponent(BoundsComponent.class))
                continue;

            TeamComponent team = plant.getComponent(TeamComponent.class);
            if (team == null || team.team != Team.PLANT)
                continue;

            BoundsComponent pb = plant.getComponent(BoundsComponent.class);
            if (!Intersector.overlaps(zRect, pb.bounds))
                continue;

            // potato mine
            if (plant.hasComponent(ArmingComponent.class)) {
                handlePotatoMine(zombie, plant);
                return;
            }

            eatingTargets.put(zombie, plant);
            zombie.setEating(true);

            float newX = pb.bounds.x + pb.bounds.width - zRect.width * 0.8f;
            zombie.setX(newX);

            damagePlantAndMaybeRemove(plant, deltaTime);
            return;
        }

        zombie.setEating(false);
    }

    private void damagePlantAndMaybeRemove(Plant plant, float deltaTime) {
        HealthComponent health = plant.getComponent(HealthComponent.class);
        if (health == null)
            return;

        float damagePerSecond = 20f;
        health.currentHealth -= damagePerSecond * deltaTime;

        if (health.isDead()) {
            plant.markedForRemoval = true;

            GridCellComponent cell = plant.getComponent(GridCellComponent.class);
            if (cell != null) {
                plantGridController.unregisterPlantAtCell(cell.row, cell.col);
            }
        }
    }

    private void handlePotatoMine(Zombies zombie, Entity potatoMine) {
        ArmingComponent arming = potatoMine.getComponent(ArmingComponent.class);

        if (arming != null && arming.isArmed) {
            ExplosiveComponent explosive = potatoMine.getComponent(ExplosiveComponent.class);
            if (explosive != null && !explosive.hasExploded) {
                explosive.fuseTime = 0;
            }
            return;
        }

        HealthComponent health = potatoMine.getComponent(HealthComponent.class);
        if (health != null) {
            health.currentHealth -= 2;
            if (health.isDead())
                potatoMine.markedForRemoval = true;
        }
    }

    private void syncBoundsWithPosition(Entity e) {
        PositionComponent pos = e.getComponent(PositionComponent.class);
        BoundsComponent bounds = e.getComponent(BoundsComponent.class);
        if (pos != null && bounds != null)
            bounds.bounds.setPosition(pos.x, pos.y);
    }

    private int getRowForZombie(Rectangle zRect) {
        float centerY = zRect.y + zRect.height / 2f;
        return GridConfig.worldToRow(centerY);
    }
}
