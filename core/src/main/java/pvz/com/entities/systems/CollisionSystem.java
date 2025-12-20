package pvz.com.entities.systems;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import pvz.com.entities.Entity;
import pvz.com.entities.zombies.BaseZombie;
import pvz.com.entities.plants.Plant;

import pvz.com.logic.PlantGridController;
import pvz.com.logic.ZombieWaveController;

import pvz.com.managers.GridConfig;
import pvz.com.managers.DesignConfig;

import pvz.com.entities.components.state.EntityState;
import pvz.com.entities.components.state.StateComponent;
import pvz.com.entities.components.team.Team;
import pvz.com.entities.components.team.TeamComponent;
import pvz.com.entities.components.tags.ProjectileTagComponent;
import pvz.com.entities.components.grid.GridCellComponent;
import pvz.com.entities.components.physics.PositionComponent;
import pvz.com.entities.components.physics.BoundsComponent;
import pvz.com.entities.components.combat.DamageComponent;
import pvz.com.entities.components.combat.HealthComponent;
import pvz.com.entities.components.combat.ArmingComponent;

public class CollisionSystem {

    private final List<Entity> entities;
    private final ZombieWaveController zombieWaveController;
    private final PlantGridController plantGridController;

    private final Map<BaseZombie, Plant> eatingTargets = new HashMap<>();

    public CollisionSystem(List<Entity> entities,
            ZombieWaveController zombieWaveController,
            PlantGridController plantGridController) {
        this.entities = entities;
        this.zombieWaveController = zombieWaveController;
        this.plantGridController = plantGridController;
    }

    public void update(float deltaTime) {
        Plant[][] grid = plantGridController.getPlantGrid();

        for (BaseZombie zombie : zombieWaveController.getzombies()) {
            if (zombie == null)
                continue;

            if (zombie.isDead()) {
                zombie.setEating(false);
                eatingTargets.remove(zombie);
                continue;
            }

            Rectangle zRect = zombie.getBounds();

            int zombieRow = getRowForZombie(zRect);

            handleZombieProjectiles(zombie, zRect);
            handleZombiePlants(zombie, zombieRow, grid, zRect, deltaTime);
        }
    }

    private void handleZombieProjectiles(BaseZombie zombie, Rectangle zRect) {
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

    private void handleZombiePlants(BaseZombie zombie,
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

            ArmingComponent arming = currentTarget.getComponent(ArmingComponent.class);
            if (arming != null && arming.isArmed) {
                handlePotatoMine(zombie, currentTarget);
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
            Rectangle plantHitbox = pb.bounds;
            if (!Intersector.overlaps(zRect, plantHitbox))
                continue;

            if (plant.hasComponent(ArmingComponent.class)) {
                boolean handled = handlePotatoMine(zombie, plant);
                if (handled)
                    return;
            }

            eatingTargets.put(zombie, plant);
            zombie.setEating(true);
            damagePlantAndMaybeRemove(plant, deltaTime);
            return;
        }

        zombie.setEating(false);
    }

    private void damagePlantAndMaybeRemove(Plant plant, float deltaTime) {
        HealthComponent health = plant.getComponent(HealthComponent.class);
        if (health == null)
            return;

        float damagePerSecond = DesignConfig.DAMAGE_PER_SECOND;
        health.currentHealth -= damagePerSecond * deltaTime;

        if (health.isDead()) {
            plant.markedForRemoval = true;
            GridCellComponent cell = plant.getComponent(GridCellComponent.class);
            if (cell != null) {
                plantGridController.unregisterPlantAtCell(cell.row, cell.col);
            }
        }
    }

    private boolean handlePotatoMine(BaseZombie zombie, Entity potatoMine) {
        ArmingComponent arming = potatoMine.getComponent(ArmingComponent.class);

        if (arming != null && !arming.isArmed) {
            return false;
        }

        if (arming != null && arming.isArmed) {
            StateComponent state = potatoMine.getComponent(StateComponent.class);

            if (state != null) {
                state.set(EntityState.EXPLODING);
            }

            return true;
        }

        return false;
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
