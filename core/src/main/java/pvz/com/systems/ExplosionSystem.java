package pvz.com.systems;

import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.List;

import pvz.com.entities.Entity;
import pvz.com.entities.Zombies.Zombies;
import pvz.com.entities.components.*;
import pvz.com.logic.PlantGridController;
import pvz.com.logic.ZombieWaveController;
import pvz.com.managers.GridConfig; // [MỚI] Import cái này để tính tọa độ

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

            // [LƯU Ý] Đã xóa dòng lấy GridPositionComponent ở đây

            if (explosive == null || state == null || pos == null)
                continue;

            // --- GIAI ĐOẠN 1: ĐẾM NGƯỢC ---
            if (!explosive.hasExploded) {
                explosive.timer += delta;

                if (explosive.timer >= explosive.fuseTime) {
                    // === KÍCH HOẠT NỔ ===
                    explosive.hasExploded = true;
                    state.set(EntityState.EXPLODING);
                    explosive.timer = 0f;

                    // 1. Xóa sự tồn tại vật lý (Hitbox & Máu)
                    entity.removeComponent(HealthComponent.class);
                    entity.removeComponent(BoundsComponent.class);

                    // 2. [FIX - QUAN TRỌNG] XÓA KHỎI GRID (Không cần GridPositionComponent)
                    // Chúng ta tính ngược từ vị trí X, Y ra Hàng và Cột
                    // Lưu ý: Phải làm bước này TRƯỚC khi dịch chuyển pos (bước 3)
                    if (plantGridController != null) {
                        // Dùng hàm tiện ích có sẵn trong GridConfig của bạn
                        int[] cell = GridConfig.worldToNearestCell(pos.x, pos.y);
                        int row = cell[0];
                        int col = cell[1];

                        // Gọi Controller xóa cây khỏi bộ nhớ
                        plantGridController.unregisterPlantAtCell(row, col);
                    }

                    // 3. Phóng to hình ảnh nổ (Logic dịch chuyển Pos)
                    if (size != null) {
                        float oldSize = size.width;
                        float newSize = 250f;
                        float offset = (newSize - oldSize) / 2f;

                        // Giờ mới dịch chuyển pos (sau khi đã tính grid ở trên)
                        pos.x -= offset;
                        pos.y -= offset;

                        size.width = newSize;
                        size.height = newSize;
                    }

                    // 4. Gây sát thương
                    dealAreaDamage(pos, size, explosive);
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

    private void dealAreaDamage(PositionComponent bombPos, SizeComponent size, ExplosiveComponent explosive) {
        float currentSize = (size != null) ? size.width : 90f;

        float centerX = bombPos.x + (currentSize / 2f);
        float centerY = bombPos.y + (currentSize / 2f);

        for (Zombies z : zombieController.getZombies()) {
            if (z.isDead())
                continue;

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
