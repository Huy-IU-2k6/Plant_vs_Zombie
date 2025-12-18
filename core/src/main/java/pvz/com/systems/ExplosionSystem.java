package pvz.com.systems;

import com.badlogic.gdx.math.Vector2;
import java.util.ArrayList;
import java.util.List;

import pvz.com.entities.Entity;
import pvz.com.entities.Zombies.BaseZombie;
import pvz.com.entities.components.*;
import pvz.com.logic.PlantGridController;
import pvz.com.logic.ZombieWaveController;
// import pvz.com.managers.GridConfig; // <-- Không cần dùng cái này để tính lại nữa

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

            if (explosive == null || state == null || pos == null)
                continue;

            // Logic kích nổ
            if (!explosive.hasExploded) {
                // ... (Phần đếm fuseTime giữ nguyên) ...
                if (explosive.fuseTime >= 0) {
                    explosive.timer += delta;
                    if (explosive.timer >= explosive.fuseTime) {
                        state.set(EntityState.EXPLODING);
                    }
                }

                if (state.get() == EntityState.EXPLODING) {
                    explosive.hasExploded = true;
                    explosive.timer = 0f;

                    // Xóa các component không cần thiết
                    entity.removeComponent(HealthComponent.class);
                    entity.removeComponent(BoundsComponent.class);

                    // =========================================================
                    // [FIX LỖI GHOST PLANT TẠI ĐÂY]
                    // =========================================================
                    if (plantGridController != null) {
                        // 1. Lấy trực tiếp GridCellComponent
                        GridCellComponent gridCell = entity.getComponent(GridCellComponent.class);
                        
                        if (gridCell != null) {
                            // Xóa đúng ô đã lưu
                            plantGridController.unregisterPlantAtCell(gridCell.row, gridCell.col);
                        } else {
                            // Fallback (chỉ dùng nếu không có component, nhưng CherryBomb chắc chắn có)
                            // System.out.println("Warning: Bomb missing GridCellComponent!");
                        }
                    }
                    // =========================================================

                    // Chỉnh lại vị trí để vẽ vụ nổ to hơn (Visual logic)
                    if (size != null) {
                        float oldSize = size.width;
                        float newSize = 250f; 
                        float offset = (newSize - oldSize) / 2f;

                        pos.x -= offset;
                        pos.y -= offset;

                        size.width = newSize;
                        size.height = newSize;
                    }

                    // Gây sát thương
                    dealAreaDamage(pos, size, explosive);
                }
            } else {
                // ... (Phần chờ animation nổ xong để remove entity giữ nguyên) ...
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
        // ... (Logic gây damage giữ nguyên) ...
        float currentSize = (size != null) ? size.width : 90f;
        float centerX = bombPos.x + (currentSize / 2f);
        float centerY = bombPos.y + (currentSize / 2f);

        for (BaseZombie z : zombieController.getZombies()) {
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