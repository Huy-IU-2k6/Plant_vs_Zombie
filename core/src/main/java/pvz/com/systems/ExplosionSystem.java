package pvz.com.systems;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.List;

import pvz.com.entities.Entity;
import pvz.com.entities.Zombies.Zombies;
import pvz.com.entities.components.*;
import pvz.com.logic.PlantGridController; // [1] Import PlantGridController
import pvz.com.logic.ZombieWaveController;

public class ExplosionSystem {
    private final ZombieWaveController zombieController;
    private final PlantGridController plantGridController; // [2] Khai báo biến

    // [3] SỬA CONSTRUCTOR: Nhận cả 2 tham số để khớp với GameScreen
    public ExplosionSystem(ZombieWaveController zombieController, PlantGridController plantGridController) {
        this.zombieController = zombieController;
        this.plantGridController = plantGridController;
    }

    public void update(List<Entity> entities, float delta) {
        List<Entity> toRemove = new ArrayList<>();

        for (Entity entity : entities) {
            ExplosiveComponent explosive = entity.getComponent(ExplosiveComponent.class);
            StateComponent state = entity.getComponent(StateComponent.class);
            PositionComponent pos = entity.getComponent(PositionComponent.class);
            AnimationComponent anim = entity.getComponent(AnimationComponent.class);
            GridPositionComponent gridPos = entity.getComponent(GridPositionComponent.class); // Lấy vị trí lưới

            if (explosive == null || state == null || pos == null) continue;

            // --- GIAI ĐOẠN 1: ĐẾM NGƯỢC ---
            if (!explosive.hasExploded) {
                explosive.timer += delta;

                if (explosive.timer >= explosive.fuseTime) {
                    explosive.hasExploded = true;
                    
                    // Chuyển sang animation BÙM
                    state.set(EntityState.EXPLODING); 
                    explosive.timer = 0f; 

                    // Gây sát thương diện rộng
                    dealAreaDamage(pos, explosive);
                    
                    // [4] QUAN TRỌNG: Xóa cây khỏi lưới ngay khi nổ
                    // Để người chơi có thể trồng cây mới vào chỗ đó ngay lập tức
                    if (gridPos != null) {
                        plantGridController.unregisterPlantAtCell(gridPos.row, gridPos.col);
                    }
                }
            } 
            // --- GIAI ĐOẠN 2: CHỜ ANIMATION NỔ XONG ---
            else {
                explosive.timer += delta;
                
                // Lấy thời gian của animation nổ
                float explodeAnimDuration = 0.8f; 
                if (anim != null && anim.getAnimation(EntityState.EXPLODING) != null) {
                    explodeAnimDuration = anim.getAnimation(EntityState.EXPLODING).getAnimationDuration();
                }

                // Nếu animation nổ đã chạy xong -> Xóa Entity khỏi game
                if (explosive.timer >= explodeAnimDuration) {
                    toRemove.add(entity); 
                }
            }
        }

        entities.removeAll(toRemove);
    }

    private void dealAreaDamage(PositionComponent bombPos, ExplosiveComponent explosive) {
        // Tâm vụ nổ (cộng thêm 45 vì kích thước bom là 90x90)
        float centerX = bombPos.x + 45f; 
        float centerY = bombPos.y + 45f;

        for (Zombies z : zombieController.getZombies()) {
            if (z.isDead()) continue;

            float zCenterX = z.getX() + z.getWidth() / 2f;
            float zCenterY = z.getY() + z.getHeight() / 2f;

            float dist = Vector2.dst(centerX, centerY, zCenterX, zCenterY);

            // Nổ trong phạm vi
            if (dist <= explosive.range) {
                // Giết ngay lập tức (kèm hiệu ứng cháy nếu có)
                z.killByCherryBomb(); 
                // Bắt buộc zombie nhả mồm ra
                z.setEating(false);   
            }
        }
    }
}