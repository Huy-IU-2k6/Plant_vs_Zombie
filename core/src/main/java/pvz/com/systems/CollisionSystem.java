package pvz.com.systems;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;

import java.util.List;

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
import pvz.com.logic.PlantGridController;
import pvz.com.logic.ZombieWaveController;
import pvz.com.managers.GridConfig;

import java.util.Map;
import java.util.HashMap;

public class CollisionSystem {

    // ECS entities: Plants, đạn, mìn,...
    private final List<Entity> entities;

    // OOP zombie controller (wave)
    private final ZombieWaveController zombieWaveController;

    // Grid cây
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

        // Duyệt từng zombie, xử lý cả: đạn trúng + ăn cây
        for (Zombies zombie : zombieWaveController.getZombies()) {
            if (zombie.isDead()) {
                zombie.setEating(false);
                continue;
            }

            // Dùng hitbox trong class Zombies
            Rectangle zRect = zombie.getBounds();
            int zombieRow = getRowForZombie(zRect);

            // 1) ZOMBIE vs PROJECTILE
            handleZombieProjectiles(zombie, zRect);

            // 2) ZOMBIE vs PLANT (GRID)
            handleZombiePlants(zombie, zombieRow, grid, zRect, deltaTime);
        }
    }

    // =========================
    // ZOMBIE vs BULLET
    // =========================
    private void handleZombieProjectiles(Zombies zombie, Rectangle zRect) {

        for (Entity projectile : entities) {
            if (projectile.markedForRemoval)
                continue;

            // Chỉ quan tâm entity là đạn
            if (projectile.getComponent(ProjectileTagComponent.class) == null)
                continue;

            BoundsComponent pBounds = projectile.getComponent(BoundsComponent.class);
            DamageComponent damage = projectile.getComponent(DamageComponent.class);
            if (pBounds == null || damage == null)
                continue;

            // Đồng bộ bounds với position mỗi frame
            syncBoundsWithPosition(projectile);

            if (!pBounds.bounds.overlaps(zRect))
                continue;

            // Trúng đạn -> trừ máu
            zombie.takeDamage(damage.amount);

            // Đạn biến mất: dùng cờ, để hệ thống cleanup chung xử lý
            projectile.markedForRemoval = true;
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

        // Ngoài lawn -> không ăn cây
        if (!GridConfig.isInsideGrid(zombieRow, 0)) {
            eatingTargets.remove(zombie);
            zombie.setEating(false);
            return;
        }

        Plant currentTarget = eatingTargets.get(zombie);

        // ========================
        // 1) ĐÃ CÓ TARGET ĐANG ĂN
        // ========================
        if (currentTarget != null) {

            // Plant đã bị xóa / chết -> bỏ ăn, đi tiếp
            if (currentTarget.markedForRemoval
                    || !currentTarget.hasComponent(HealthComponent.class)) {
                eatingTargets.remove(zombie);
                zombie.setEating(false);
                return;
            }

            // Optional: nếu muốn kiểm tra vẫn còn overlaps
            BoundsComponent pb = currentTarget.getComponent(BoundsComponent.class);
            if (pb == null || !Intersector.overlaps(zRect, pb.bounds)) {
                // Có thể bỏ check này nếu muốn zombie cứ đứng ăn đến khi plant chết
                // Còn nếu giữ thì khi hết overlaps cũng cho nó đi tiếp
                eatingTargets.remove(zombie);
                zombie.setEating(false);
                return;
            }

            // Vẫn còn target -> tiếp tục ăn nó
            zombie.setEating(true);
            damagePlantAndMaybeRemove(currentTarget, deltaTime);

            // Nếu plant chết trong damagePlantAndMaybeRemove, vòng sau sẽ vào nhánh remove
            // ở trên
            return;
        }

        // ===========================
        // 2) CHƯA CÓ TARGET -> TÌM MỚI
        // ===========================
        Plant[] rowPlants = grid[zombieRow];

        for (Plant plant : rowPlants) {
            if (plant == null || plant.markedForRemoval)
                continue;
            if (!plant.hasComponent(BoundsComponent.class))
                continue;
            if (!plant.hasComponent(TeamComponent.class))
                continue;

            TeamComponent team = plant.getComponent(TeamComponent.class);
            if (team.team != Team.PLANT)
                continue;

            BoundsComponent pb = plant.getComponent(BoundsComponent.class);
            if (!Intersector.overlaps(zRect, pb.bounds))
                continue;

            // ==== POTATO MINE ====
            if (plant.hasComponent(ArmingComponent.class)) {
                handlePotatoMine(zombie, plant);
                // Mine nổ xong thì thường zombie chết hoặc bị damage ở system khác.
                // Ở đây không set target ăn mìn (optional, tùy thiết kế).
                return;
            }

            // ==== CÂY THƯỜNG: CHỌN LÀM TARGET ====
            eatingTargets.put(zombie, plant);
            zombie.setEating(true);

            // "Chốt" vị trí zombie sát mép plant để tránh lệch bounding box
            // (tùy hướng di chuyển, mình giả sử zombie đi từ phải sang trái)
            float newX = pb.bounds.x + pb.bounds.width - zRect.width * 0.8f;
            zombie.setX(newX);

            // Cắn cây ngay frame này luôn
            damagePlantAndMaybeRemove(plant, deltaTime);
            return;
        }

        // Không đụng cây nào & không có target -> đảm bảo zombie không ăn
        zombie.setEating(false);
    }

    // =========================
    // PLANT DAMAGE
    // =========================
    private void damagePlantAndMaybeRemove(Plant plant, float deltaTime) {
        HealthComponent health = plant.getComponent(HealthComponent.class);
        if (health == null)
            return;

        // Có thể scale theo thời gian: 20 máu / giây chẳng hạn
        float damagePerSecond = 20f;
        health.currentHealth -= damagePerSecond * deltaTime;

        if (health.isDead()) {
            plant.markedForRemoval = true;

            // Dọn ô trong grid
            GridCellComponent cell = plant.getComponent(GridCellComponent.class);
            if (cell != null) {
                plantGridController.unregisterPlantAtCell(cell.row, cell.col);
            }
        }
    }

    // =========================
    // POTATO MINE
    // =========================
    private void handlePotatoMine(Zombies zombie, Entity potatoMine) {
        ArmingComponent arming = potatoMine.getComponent(ArmingComponent.class);

        // CASE 1: Đã trồi lên (Armed) -> BÙM!
        if (arming != null && arming.isArmed) {
            ExplosiveComponent explosive = potatoMine.getComponent(ExplosiveComponent.class);
            if (explosive != null && !explosive.isExploded) {
                // Kích hoạt nổ ngay lập tức (Fuse = 0)
                explosive.fuseTimer = 0;
                // ExplosionSystem sẽ lo phần gây damage diện rộng và xóa mìn ở vòng sau
            }
            return;
        }

        // CASE 2: Chưa trồi lên -> Bị ăn như cây thường
        HealthComponent health = potatoMine.getComponent(HealthComponent.class);
        if (health != null) {
            // Zombie ăn nhanh hơn chút
            health.currentHealth -= 2;
            if (health.isDead()) {
                potatoMine.markedForRemoval = true;
            }
        }
    }

    // =========================
    // UTIL
    // =========================
    private void syncBoundsWithPosition(Entity e) {
        PositionComponent pos = e.getComponent(PositionComponent.class);
        BoundsComponent bounds = e.getComponent(BoundsComponent.class);
        if (pos != null && bounds != null) {
            bounds.bounds.setPosition(pos.x, pos.y);
        }
    }

    private int getRowForZombie(Rectangle zRect) {
        float centerY = zRect.y + zRect.height / 2f;
        return GridConfig.worldToRow(centerY);
    }
}
