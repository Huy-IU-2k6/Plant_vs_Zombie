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
import pvz.com.logic.PlantGridController;
import pvz.com.logic.ZombieWaveController;
import pvz.com.managers.GridConfig;

public class CollisionSystem {

    private final List<Entity> entities;
    private final ZombieWaveController zombieWaveController;
    private final PlantGridController plantGridController;

    private final Map<Zombies, Plant> eatingTargets = new HashMap<>();

    // [FIX 1] Constructor rút gọn còn 3 tham số (Bỏ GameState)
    // Để khớp với code gọi bên GameScreen/GameWorld
    public CollisionSystem(List<Entity> entities,
                           ZombieWaveController zombieWaveController,
                           PlantGridController plantGridController) {
        this.entities = entities;
        this.zombieWaveController = zombieWaveController;
        this.plantGridController = plantGridController;
    }

    public void update(float deltaTime) {
        Plant[][] grid = plantGridController.getPlantGrid();

        // Mép thua cuộc (cổng nhà) - Chỉ dùng để tham khảo, GameWorld sẽ check việc thua
        float loseX = GridConfig.getCellOriginX(0) - GridConfig.CELL_WIDTH * 0.35f;

        for (Zombies zombie : zombieWaveController.getZombies()) {
            if (zombie == null) continue;

            if (zombie.isDead()) {
                zombie.setEating(false);
                eatingTargets.remove(zombie);
                continue;
            }

            Rectangle zRect = zombie.getBounds();

            // Check Game Over (Chỉ in log hoặc xử lý logic khác vì không có biến GameState ở đây)
            // Việc setGameOver sẽ do GameWorld đảm nhận
            if (zRect.x <= loseX) {
                // zombie.setEating(false);
                // return; 
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

        // 1. Kiểm tra cây đang ăn dở (nếu có)
        Plant currentTarget = eatingTargets.get(zombie);
        if (currentTarget != null) {
            // Nếu cây đã bị xóa hoặc mất component máu -> Dừng ăn
            if (currentTarget.markedForRemoval || !currentTarget.hasComponent(HealthComponent.class)) {
                eatingTargets.remove(zombie);
                zombie.setEating(false);
                return;
            }

            // Nếu zombie đi quá xa khỏi cây -> Dừng ăn
            BoundsComponent pb = currentTarget.getComponent(BoundsComponent.class);
            if (pb == null || !Intersector.overlaps(zRect, pb.bounds)) {
                eatingTargets.remove(zombie);
                zombie.setEating(false);
                return;
            }

            // [FIX QUAN TRỌNG] Kiểm tra lại nếu cây này bỗng nhiên biến thành Potato Mine chưa chín (hiếm gặp nhưng an toàn)
            ArmingComponent arming = currentTarget.getComponent(ArmingComponent.class);
            if (arming != null && !arming.isArmed) {
                eatingTargets.remove(zombie);
                zombie.setEating(false);
                return;
            }

            // Tiếp tục ăn
            zombie.setEating(true);
            damagePlantAndMaybeRemove(currentTarget, deltaTime);
            return;
        }

        // 2. Tìm cây mới trong cùng hàng
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
            
            // Tinh chỉnh vùng va chạm để zombie đi sâu vào cây một chút mới ăn
            Rectangle plantHitbox = pb.bounds;
            if (!Intersector.overlaps(zRect, plantHitbox))
                continue;

            // [FIX 2] Xử lý Potato Mine
            if (plant.hasComponent(ArmingComponent.class)) {
                boolean handled = handlePotatoMine(zombie, plant);
                if (handled) return; // Nếu là mìn, xử lý xong thì return (không ăn)
                // Nếu handled = false (ví dụ mìn đã nổ xong), code chạy tiếp xuống dưới
            }

            // Bắt đầu ăn cây (Không phải Potato Mine chưa chín)
            eatingTargets.put(zombie, plant);
            zombie.setEating(true);

            damagePlantAndMaybeRemove(plant, deltaTime);
            return;
        }

        // Không gặp cây nào
        zombie.setEating(false);
    }

    private void damagePlantAndMaybeRemove(Plant plant, float deltaTime) {
        HealthComponent health = plant.getComponent(HealthComponent.class);
        if (health == null) return;

        float damagePerSecond = 100f; // Tốc độ ăn của Zombie
        health.currentHealth -= damagePerSecond * deltaTime;

        if (health.isDead()) {
            plant.markedForRemoval = true;
            GridCellComponent cell = plant.getComponent(GridCellComponent.class);
            if (cell != null) {
                plantGridController.unregisterPlantAtCell(cell.row, cell.col);
            }
        }
    }

    // [FIX 3] Logic Potato Mine chuẩn: Return true nếu đã xử lý va chạm
    private boolean handlePotatoMine(Zombies zombie, Entity potatoMine) {
        ArmingComponent arming = potatoMine.getComponent(ArmingComponent.class);

        // Trường hợp 1: Mìn chưa chín -> Zombie đi xuyên qua (IGNORE)
        // Return true để báo hiệu "Đã xử lý xong, đừng cho zombie ăn cây này"
        if (arming != null && !arming.isArmed) {
            return true; 
        }

        // Trường hợp 2: Mìn đã chín -> Kích hoạt nổ
        if (arming != null && arming.isArmed) {
            ExplosiveComponent explosive = potatoMine.getComponent(ExplosiveComponent.class);
            StateComponent state = potatoMine.getComponent(StateComponent.class);
            
            // Kích hoạt nổ bằng cách set State = EXPLODING
            // ExplosionSystem sẽ lo phần còn lại (gây dam, xóa cây)
            if (state != null) {
                state.set(EntityState.EXPLODING);
            }
            
            // Return true để zombie không dừng lại ăn mìn (nó sẽ chết vì nổ)
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