package pvz.com.systems;

import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.List;

import pvz.com.entities.Entity;
import pvz.com.entities.Zombies.BaseZombie;
import pvz.com.entities.components.*;
import pvz.com.logic.PlantGridController;
import pvz.com.logic.ZombieWaveController;
import pvz.com.managers.GridConfig;

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
            SizeComponent size = entity.getComponent(SizeComponent.class);

            if (explosive == null || state == null || pos == null) continue;

            // --- GIAI ĐOẠN 1: CHỜ KÍCH NỔ (Đếm ngược HOẶC Chờ đạp trúng) ---
            if (!explosive.hasExploded) {
                
                // A. XỬ LÝ ĐẾM NGƯỢC (Chỉ dành cho bom hẹn giờ như CherryBomb)
                // Nếu fuseTime < 0 (PotatoMine) thì bỏ qua đoạn này
                if (explosive.fuseTime >= 0) {
                    explosive.timer += delta;
                    if (explosive.timer >= explosive.fuseTime) {
                        // Hết giờ -> Ép trạng thái sang EXPLODING
                        state.set(EntityState.EXPLODING);
                    }
                }

                // B. KÍCH HOẠT NỔ THỰC SỰ
                // Điều kiện: State đã chuyển sang EXPLODING (do hết giờ Ở TRÊN hoặc do CollisionSystem SET)
                if (state.get() == EntityState.EXPLODING) {
                    
                    // Đánh dấu đã nổ để không chạy lại đoạn này nữa
                    explosive.hasExploded = true;
                    
                    // Reset timer về 0 để dùng cho việc đếm thời gian Animation nổ (Giai đoạn 2)
                    explosive.timer = 0f; 

                    // 1. Xóa sự tồn tại vật lý
                    entity.removeComponent(HealthComponent.class);
                    entity.removeComponent(BoundsComponent.class);

                    // 2. XÓA KHỎI GRID (Tính toán trước khi dịch chuyển hình ảnh)
                    if (plantGridController != null) {
                        // Tính ra hàng/cột dựa trên tọa độ hiện tại
                        int[] cell = GridConfig.worldToNearestCell(pos.x, pos.y);
                        int row = cell[0];
                        int col = cell[1];
                        
                        // Báo cho controller biết ô này đã trống
                        plantGridController.unregisterPlantAtCell(row, col);
                    }

                    // 3. Phóng to hình ảnh nổ (Hiệu ứng bùm to hơn cây)
                    if (size != null) {
                        float oldSize = size.width;
                        float newSize = 250f;       
                        float offset = (newSize - oldSize) / 2f; 
                        
                        pos.x -= offset;
                        pos.y -= offset;

                        size.width = newSize;
                        size.height = newSize;
                    }

                    // 4. Gây sát thương diện rộng
                    dealAreaDamage(pos, size, explosive);
                }
            } 
            // --- GIAI ĐOẠN 2: CHỜ ANIMATION NỔ XONG RỒI XÓA ENTITY ---
            else {
                explosive.timer += delta;
                
                // Mặc định nổ trong 0.8s nếu không tìm thấy animation
                float explodeAnimDuration = 0.8f; 
                
                // Lấy thời gian thực của Animation EXPLODING (nếu có)
                if (anim != null && anim.getAnimation(EntityState.EXPLODING) != null) {
                    explodeAnimDuration = anim.getAnimation(EntityState.EXPLODING).getAnimationDuration();
                }

                // Chạy hết phim nổ thì xóa khỏi game
                if (explosive.timer >= explodeAnimDuration) {
                    toRemove.add(entity);
                }
            }
        }

        entities.removeAll(toRemove);
    }

    private void dealAreaDamage(PositionComponent bombPos, SizeComponent size, ExplosiveComponent explosive) {
        float currentSize = (size != null) ? size.width : 90f; 
        
        // Tính tâm vụ nổ
        float centerX = bombPos.x + (currentSize / 2f); 
        float centerY = bombPos.y + (currentSize / 2f);

        for (BaseZombie z : zombieController.getZombies()) {
            if (z.isDead())
                continue;

            float zCenterX = z.getX() + z.getWidth() / 2f;
            float zCenterY = z.getY() + z.getHeight() / 2f;

            float dist = Vector2.dst(centerX, centerY, zCenterX, zCenterY);

            // Nếu nằm trong bán kính nổ
            if (dist <= explosive.range) {
                // PotatoMine damage rất to (1800), giết được hầu hết zombie
                // Hàm killByCherryBomb() thường xử lý việc biến thành tro đen
                z.killByCherryBomb(); 
                z.setEating(false); 
            }
        }
    }
}
