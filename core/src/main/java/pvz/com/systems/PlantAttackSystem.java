package pvz.com.systems;

import java.util.List;

import pvz.com.entities.Entity;
import pvz.com.entities.plants.Plant;
import pvz.com.entities.components.*;
import pvz.com.logic.ZombieWaveController;
import pvz.com.entities.Zombies.Zombies;
import pvz.com.managers.DesignConfig;

public class PlantAttackSystem {
    private final IGameSpawner spawner;
    private final ZombieWaveController zombieController;

    public PlantAttackSystem(IGameSpawner spawner, ZombieWaveController zombieController) {
        this.spawner = spawner;
        this.zombieController = zombieController;
    }

    public void update(List<Plant> plants, float delta) {
        for (Plant plant : plants) {
            PlantAttackComponent attack = plant.getComponent(PlantAttackComponent.class);
            PositionComponent pos = plant.getComponent(PositionComponent.class);

            if (attack == null || pos == null) continue;

            // Tăng bộ đếm giờ
            attack.timer += delta;

            // =========================================================
            // LOGIC BẮN (BURST FIRE)
            // =========================================================
            
            // TRƯỜNG HỢP 1: Đang trong trạng thái hồi chiêu (Chưa bắt đầu loạt mới)
            if (attack.shotsFiredInBurst == 0) {
                // Kiểm tra xem đã hồi chiêu xong chưa
                if (attack.timer >= attack.attackSpeed) {
                    // Kiểm tra xem có Zombie để bắn không
                    if (shouldShoot(pos, attack.range)) {
                        shoot(plant, attack, pos);
                        attack.shotsFiredInBurst++; // Đã bắn viên 1
                        attack.timer = 0f;          // Reset timer để đếm burstDelay
                    }
                }
            } 
            // TRƯỜNG HỢP 2: Đang bắn dở loạt (Repeater đang bắn viên thứ 2)
            else if (attack.shotsFiredInBurst < attack.burstCount) {
                // Kiểm tra delay ngắn giữa các viên đạn (ví dụ 0.15s)
                if (attack.timer >= attack.burstDelay) {
                     // Kiểm tra lại xem Zombie còn đó không (nếu chết hết thì thôi không bắn phí đạn)
                     if (shouldShoot(pos, attack.range)) {
                        shoot(plant, attack, pos);
                        attack.shotsFiredInBurst++;
                        attack.timer = 0f;
                     } else {
                         // Không còn mục tiêu -> Hủy loạt bắn, reset về chờ hồi chiêu
                         attack.shotsFiredInBurst = 0;
                         attack.timer = 0f; 
                     }
                }
            } 
            // TRƯỜNG HỢP 3: Đã bắn xong loạt
            else {
                // Reset về 0 để bắt đầu tính thời gian hồi chiêu lớn (attackSpeed)
                attack.shotsFiredInBurst = 0;
                // Không reset timer ở đây, để nó tiếp tục đếm cho đến attackSpeed
            }
        }
    }

    private void shoot(Plant plant, PlantAttackComponent attack, PositionComponent pos) {
        // [Logic bắn giữ nguyên]
        // Kích hoạt animation ATTACK cho cây (nếu có)
        StateComponent state = plant.getComponent(StateComponent.class);
        if (state != null) {
            state.set(EntityState.ATTACKING);
            // Reset state về IDLE sau 1 khoảng thời gian (xử lý ở AnimationSystem)
        }

        // Spawn đạn
        float spawnX = pos.x + 40f; 
        float spawnY = pos.y + 35f; 
        spawner.spawnProjectile(spawnX, spawnY, attack.damage, attack.damageType, attack.projectileClass);
    }

    private boolean shouldShoot(PositionComponent plantPos, float range) {
        if (zombieController == null) return false;
        
        float screenRightEdge = DesignConfig.BASE_SCREEN_W; 
        
        for (Zombies z : zombieController.getZombies()) {
            if (z.isDead() || z.getHealth() <= 0) continue; 
            
            // Kiểm tra làn đường (Row)
            // (Giả sử bạn check làn đường bằng Y hoặc GridPosition, ở đây demo check Y)
            if (Math.abs(z.getY() - plantPos.y) > 50f) continue; 

            // Kiểm tra phía trước mặt
            if (z.getX() > plantPos.x && (z.getX() - plantPos.x) <= range) {
                return true; 
            }
        }
        return false;
    }
}