package pvz.com.systems;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.List;

import pvz.com.entities.Entity;
import pvz.com.entities.Zombies.Zombies;
import pvz.com.entities.components.*;
import pvz.com.logic.PlantGridController;
import pvz.com.logic.ZombieWaveController;

public class ExplosionSystem {
    private final ZombieWaveController zombieController;
    private final PlantGridController plantGridController;

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
            GridPositionComponent gridPos = entity.getComponent(GridPositionComponent.class);
            
            // [MỚI] Lấy thêm SizeComponent để thay đổi kích thước hiển thị
            SizeComponent size = entity.getComponent(SizeComponent.class);

            if (explosive == null || state == null || pos == null) continue;

            // --- GIAI ĐOẠN 1: ĐẾM NGƯỢC ---
            if (!explosive.hasExploded) {
                explosive.timer += delta;

                if (explosive.timer >= explosive.fuseTime) {
                    explosive.hasExploded = true;
                    
                    // 1. Chuyển trạng thái sang BÙM
                    state.set(EntityState.EXPLODING); 
                    explosive.timer = 0f; 

                    // =========================================================
                    // 2. [QUAN TRỌNG] LOGIC PHÓNG TO VISUAL
                    // =========================================================
                    if (size != null) {
                        float oldSize = size.width; // Kích thước cũ (90)
                        float newSize = 250f;       // Kích thước nổ mong muốn (To hơn)
                        
                        // Tính độ lệch để căn giữa: (250 - 90) / 2 = 80
                        float offset = (newSize - oldSize) / 2f; 

                        // Dịch chuyển vị trí lùi lại (lên trên, sang trái)
                        pos.x -= offset;
                        pos.y -= offset;

                        // Cập nhật kích thước mới để RenderSystem vẽ hình to ra
                        size.width = newSize;
                        size.height = newSize;
                    }

                    // 3. Gây sát thương (Tính toán lại tâm dựa trên kích thước mới)
                    dealAreaDamage(pos, size, explosive);
                    
                    // 4. Xóa cây khỏi lưới trồng (để trồng cây mới đc ngay)
                    if (gridPos != null) {
                        plantGridController.unregisterPlantAtCell(gridPos.row, gridPos.col);
                    }
                }
            } 
            // --- GIAI ĐOẠN 2: CHỜ ANIMATION NỔ XONG ---
            else {
                explosive.timer += delta;
                
                float explodeAnimDuration = 0.8f; 
                if (anim != null && anim.getAnimation(EntityState.EXPLODING) != null) {
                    explodeAnimDuration = anim.getAnimation(EntityState.EXPLODING).getAnimationDuration();
                }

                if (explosive.timer >= explodeAnimDuration) {
                    toRemove.add(entity); 
                }
            }
        }

        entities.removeAll(toRemove);
    }

    // [CẬP NHẬT] Hàm tính sát thương nhận thêm SizeComponent để tính tâm chuẩn
    private void dealAreaDamage(PositionComponent bombPos, SizeComponent size, ExplosiveComponent explosive) {
        float currentSize = (size != null) ? size.width : 90f; // Nếu ko có size thì mặc định 90
        
        // Tính tâm dựa trên kích thước hiện tại (đã phóng to)
        float centerX = bombPos.x + (currentSize / 2f); 
        float centerY = bombPos.y + (currentSize / 2f);

        for (Zombies z : zombieController.getZombies()) {
            if (z.isDead()) continue;

            float zCenterX = z.getX() + z.getWidth() / 2f;
            float zCenterY = z.getY() + z.getHeight() / 2f;

            float dist = Vector2.dst(centerX, centerY, zCenterX, zCenterY);

            if (dist <= explosive.range) {
                z.killByCherryBomb(); 
                z.setEating(false);   
            }
        }
    }
}