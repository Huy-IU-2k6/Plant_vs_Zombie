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
            
            // TRƯỜNG HỢP 1: Đang trong trạng thái hồi chiêu
            if (attack.shotsFiredInBurst == 0) {
                if (attack.timer >= attack.attackSpeed) {
                    if (shouldShoot(pos, attack.range)) {
                        shoot(plant, attack, pos);
                        attack.shotsFiredInBurst++; 
                        attack.timer = 0f;          
                    }
                }
            } 
            // TRƯỜNG HỢP 2: Đang bắn dở loạt (Repeater)
            else if (attack.shotsFiredInBurst < attack.burstCount) {
                if (attack.timer >= attack.burstDelay) {
                      if (shouldShoot(pos, attack.range)) {
                        shoot(plant, attack, pos);
                        attack.shotsFiredInBurst++;
                        attack.timer = 0f;
                      } else {
                          attack.shotsFiredInBurst = 0;
                          attack.timer = 0f; 
                      }
                }
            } 
            // TRƯỜNG HỢP 3: Đã bắn xong loạt
            else {
                attack.shotsFiredInBurst = 0;
            }
        }
    }

    private void shoot(Plant plant, PlantAttackComponent attack, PositionComponent pos) {
        // [FIX LỖI ĐỨNG IM]
        // Mình đã comment dòng này lại. 
        // Cây sẽ giữ nguyên trạng thái IDLE (đang lắc lư) nên sẽ không bị khựng lại.
        
        /* StateComponent state = plant.getComponent(StateComponent.class);
        if (state != null) {
            state.set(EntityState.ATTACKING);
        }
        */

        // Spawn đạn
        float spawnX = pos.x + 40f; 
        float spawnY = pos.y + 35f; 
        spawner.spawnProjectile(spawnX, spawnY, attack.damage, attack.damageType, attack.projectileClass);
    }

    // [FIX LỖI BẮN SỚM] Thêm logic kiểm tra Zombie vào sân
    private boolean shouldShoot(PositionComponent plantPos, float range) {
        if (zombieController == null) return false;
        
        float screenRightEdge = DesignConfig.BASE_SCREEN_W; 
        
        // Khoảng cách an toàn: Zombie phải đi qua mép phải 60px mới bị bắn
        float safeMargin = 60f; 

        for (Zombies z : zombieController.getZombies()) {
            if (z.isDead() || z.getHealth() <= 0) continue; 
            
            // 1. [QUAN TRỌNG] Nếu Zombie chưa đi vào sân (vẫn ở ngoài mép phải) -> Bỏ qua
            if (z.getX() > (screenRightEdge - safeMargin)) continue;
            
            // 2. Kiểm tra làn đường (Row)
            if (Math.abs(z.getY() - plantPos.y) > 50f) continue; 

            // 3. Kiểm tra phía trước mặt & Tầm bắn
            if (z.getX() > plantPos.x && (z.getX() - plantPos.x) <= range) {
                return true; 
            }
        }
        return false;
    }
}