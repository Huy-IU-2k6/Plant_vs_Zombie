package pvz.com.systems;

import com.badlogic.gdx.math.Vector2;
import pvz.com.entities.Entity;
import pvz.com.entities.components.*;

import java.util.List;

public class ExplosionSystem {
    private List<Entity> entities;

    // Cần danh sách toàn bộ entity để quét xem ai đứng gần bom
    public ExplosionSystem(List<Entity> entities) {
        this.entities = entities;
    }

    public void update(float deltaTime) {
        for (Entity entity : entities) {
            // Chỉ xử lý những entity có khả năng nổ
            if (entity.hasComponent(ExplosiveComponent.class)) {
                processExplosive(entity, deltaTime);
            }
        }
    }

    private void processExplosive(Entity bombEntity, float deltaTime) {
        ExplosiveComponent explosive = bombEntity.getComponent(ExplosiveComponent.class);
        
        // Nếu đã nổ rồi thì bỏ qua (chờ CleanupSystem xóa)
        if (explosive.isExploded) return;

        // 1. Đếm ngược
        explosive.fuseTimer -= deltaTime;

        // 2. Kích hoạt nổ
        if (explosive.fuseTimer <= 0) {
            triggerExplosion(bombEntity, explosive);
        }
    }

    private void triggerExplosion(Entity bombEntity, ExplosiveComponent explosive) {
        explosive.isExploded = true; // Đánh dấu đã nổ

        // Lấy vị trí tâm của quả bom (để tính khoảng cách cho chuẩn)
        PositionComponent bombPos = bombEntity.getComponent(PositionComponent.class);
        SizeComponent bombSize = bombEntity.getComponent(SizeComponent.class);
        
        float bombCenterX = bombPos.x + bombSize.width / 2;
        float bombCenterY = bombPos.y + bombSize.height / 2;
        Vector2 center = new Vector2(bombCenterX, bombCenterY);

        // 3. Quét tất cả entity để tìm nạn nhân
        for (Entity victim : entities) {
            // Bỏ qua chính quả bom
            if (victim == bombEntity) continue;

            // Chỉ gây sát thương cho ZOMBIE (Team check)
            if (victim.hasComponent(TeamComponent.class)) {
                TeamComponent team = victim.getComponent(TeamComponent.class);
                if (team.team != Team.ZOMBIE) continue; // Không nổ quân ta
            } else {
                continue; // Không có phe thì không đánh
            }

            // Kiểm tra khoảng cách
            if (victim.hasComponent(PositionComponent.class) && victim.hasComponent(SizeComponent.class)) {
                PositionComponent vicPos = victim.getComponent(PositionComponent.class);
                SizeComponent vicSize = victim.getComponent(SizeComponent.class);

                // Lấy tâm của Zombie
                float vicCenterX = vicPos.x + vicSize.width / 2;
                float vicCenterY = vicPos.y + vicSize.height / 2;

                // Tính khoảng cách giữa Bom và Zombie
                float distance = center.dst(vicCenterX, vicCenterY);

                // Nếu nằm trong vùng nổ -> Gây sát thương
                if (distance <= explosive.range) {
                    applyDamage(victim, explosive.damage);
                }
            }
        }

        // 4. Xóa quả bom sau khi nổ
        bombEntity.markedForRemoval = true;
        
        // TODO: Ở đây bạn nên spawn một Entity mới là "ExplosionEffect" 
        // để hiển thị hình ảnh vụ nổ "POW!" (nếu không bom biến mất cái bộp rất kỳ)
        // createExplosionEffect(bombCenterX, bombCenterY);
    }

    private void applyDamage(Entity victim, int damage) {
        if (victim.hasComponent(HealthComponent.class)) {
            HealthComponent health = victim.getComponent(HealthComponent.class);
            health.currentHealth -= damage;
            
            // Debug log để biết zombie đã bị nổ banh xác
            System.out.println("ZOMBIE TOOK " + damage + " EXPLOSIVE DAMAGE! HP: " + health.currentHealth);
        }
    }
}