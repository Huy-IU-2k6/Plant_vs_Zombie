package pvz.com.systems;

import java.util.List;

import pvz.com.entities.Zombies.Zombies;
import pvz.com.entities.components.HealthComponent;
import pvz.com.entities.components.PlantAttackComponent;
import pvz.com.entities.components.PositionComponent;
import pvz.com.entities.plants.Plant;
import pvz.com.logic.ZombieWaveController;
import pvz.com.managers.DesignConfig;

public class PlantAttackSystem {

    // ===== TUNING =====
    private static final float LANE_Y_TOLERANCE = 50f;

    // Spawn offset (tùy sprite plant)
    private static final float PROJECTILE_SPAWN_OFFSET_X = 20f;
    private static final float PROJECTILE_SPAWN_OFFSET_Y = 50f;

    // Zombie phải “vào màn” rồi mới cho plant bắn (đỡ bắn từ ngoài màn hình)
    private static final float ZOMBIE_ENTER_SCREEN_MARGIN = 100f;

    // Tăng cooldown: > 1.0 = bắn chậm hơn, < 1.0 = bắn nhanh hơn
    private static final float COOLDOWN_MULTIPLIER = 3.0f;

    private final IGameSpawner spawner;
    private final ZombieWaveController zombieController;

    public PlantAttackSystem(IGameSpawner spawner, ZombieWaveController zombieController) {
        this.spawner = spawner;
        this.zombieController = zombieController;
    }

    public void update(List<Plant> plants, float deltaTime) {
        if (plants == null || spawner == null || zombieController == null)
            return;

        for (Plant plant : plants) {
            if (plant == null)
                continue;

            // chết thì bỏ qua
            HealthComponent hp = plant.getComponent(HealthComponent.class);
            if (hp != null && hp.currentHealth <= 0)
                continue;

            PlantAttackComponent atk = plant.getComponent(PlantAttackComponent.class);
            PositionComponent pos = plant.getComponent(PositionComponent.class);
            if (atk == null || pos == null || atk.cooldown == null)
                continue;

            // cooldown hiệu dụng
            float effectiveCooldown = atk.cooldown.cooldownTime * COOLDOWN_MULTIPLIER;

            // tích thời gian
            atk.cooldown.timer += deltaTime;

            // chưa tới nhịp bắn
            if (atk.cooldown.timer < effectiveCooldown)
                continue;

            // tới nhịp bắn: chỉ bắn nếu có zombie hợp lệ
            if (shouldShoot(pos, atk.range)) {
                atk.cooldown.timer = 0f;

                spawner.spawnProjectile(
                        pos.x + PROJECTILE_SPAWN_OFFSET_X,
                        pos.y + PROJECTILE_SPAWN_OFFSET_Y,
                        atk.damage,
                        atk.damageType,
                        atk.projectileType);
            } else {
                // giữ ở ngưỡng để “ready-to-shoot”
                atk.cooldown.timer = effectiveCooldown;
            }
        }
    }

    // Kiểm tra có zombie “đủ điều kiện” để bắn không
    private boolean shouldShoot(PositionComponent plantPos, float range) {
        if (zombieController == null || zombieController.getZombies() == null)
            return false;

        // rìa phải của màn (theo layout gốc)
        float screenRightEdge = DesignConfig.BASE_SCREEN_W;

        for (Zombies z : zombieController.getZombies()) {
            if (z == null)
                continue;
            if (z.isDead() || z.getHealth() <= 0)
                continue;

            // zombie còn ở ngoài màn (đang spawn ngoài phải) thì bỏ qua
            if (z.getX() > (screenRightEdge - ZOMBIE_ENTER_SCREEN_MARGIN))
                continue;

            // check lane
            if (Math.abs(z.getY() - plantPos.y) > LANE_Y_TOLERANCE)
                continue;

            // check range: chỉ bắn zombie ở phía trước
            float dx = z.getX() - plantPos.x;
            if (dx > 0f && dx <= range)
                return true;
        }

        return false;
    }
}
