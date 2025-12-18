package pvz.com.systems;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import pvz.com.entities.Entity;
import pvz.com.entities.Zombies.BaseZombie;
import pvz.com.entities.components.*;
import pvz.com.entities.plants.Plant;
import pvz.com.logic.PlantGridController;
import pvz.com.logic.ZombieWaveController;
import pvz.com.managers.GridConfig;
import pvz.com.managers.DesignConfig;

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
        float loseX = GridConfig.getCellOriginX(0) - GridConfig.CELL_WIDTH * 0.35f;

        for (BaseZombie zombie : zombieWaveController.getZombies()) {
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

        // 1. Kiểm tra cây đang ăn dở
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

            // Kiểm tra lại nếu cây là Potato Mine
            ArmingComponent arming = currentTarget.getComponent(ArmingComponent.class);
            // [LOGIC GỐC]: Nếu mìn chưa chín -> Vẫn tiếp tục ăn
            // Nếu mìn ĐÃ CHÍN -> Ngừng ăn để kích hoạt nổ (trường hợp hiếm khi đang ăn thì
            // chín)
            if (arming != null && arming.isArmed) {
                // Kích hoạt nổ ngay lập tức
                handlePotatoMine(zombie, currentTarget);
                eatingTargets.remove(zombie);
                zombie.setEating(false);
                return;
            }

            zombie.setEating(true);
            damagePlantAndMaybeRemove(currentTarget, deltaTime);
            return;
        }

        // 2. Tìm cây mới
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

            // Xử lý Potato Mine
            if (plant.hasComponent(ArmingComponent.class)) {
                boolean handled = handlePotatoMine(zombie, plant);
                // Nếu handled = true (đã kích nổ) -> return
                // Nếu handled = false (chưa chín) -> chạy tiếp xuống dưới để zombie ăn
                if (handled)
                    return;
            }

            // Bắt đầu ăn cây (Bao gồm cả Potato Mine chưa chín)
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

        // Có thể thay bằng DesignConfig.DAMAGE_PER_SECOND nếu có
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

    // [FIXED LOGIC]
    private boolean handlePotatoMine(BaseZombie zombie, Entity potatoMine) {
        ArmingComponent arming = potatoMine.getComponent(ArmingComponent.class);

        // Trường hợp 1: Mìn CHƯA CHÍN (Unarmed)
        if (arming != null && !arming.isArmed) {
            // [QUAN TRỌNG] Return FALSE
            // Nghĩa là: "CollisionSystem chưa xử lý xong, hãy coi như cây bình thường"
            // -> Zombie sẽ ăn cây này ở đoạn code phía dưới.
            return false;
        }

        // Trường hợp 2: Mìn ĐÃ CHÍN (Armed) -> Kích hoạt nổ
        if (arming != null && arming.isArmed) {
            StateComponent state = potatoMine.getComponent(StateComponent.class);

            // Set state nổ -> ExplosionSystem sẽ xử lý gây damage
            if (state != null) {
                state.set(EntityState.EXPLODING);
            }

            // Return TRUE: "Đã xử lý xong (nổ rồi), Zombie không cần làm gì nữa"
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
