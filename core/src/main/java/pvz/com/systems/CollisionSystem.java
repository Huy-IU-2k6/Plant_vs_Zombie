package pvz.com.systems;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import pvz.com.entities.Entity;
import pvz.com.entities.Zombies.NormalZombie;
import pvz.com.entities.components.*;
import pvz.com.entities.projectiles.PeaProjectile;

import java.util.List;

public class CollisionSystem {
    private List<Entity> entities;       // List chứa Cây, Đạn, Mìn (ECS)
    private Array<NormalZombie> zombies; // List chứa Zombie (OOP cũ)

    public CollisionSystem(List<Entity> entities, Array<NormalZombie> zombies) {
        this.entities = entities;
        this.zombies = zombies;
    }

    public void update(float deltaTime) {
        // Duyệt qua tất cả Zombie đang sống
        for (NormalZombie zombie : zombies) {
            // Lấy hình chữ nhật va chạm của Zombie
            // (Giả sử NormalZombie có hàm getBounds hoặc bạn tự tạo Rect từ x,y,width,height)
            Rectangle zombieBounds = new Rectangle(zombie.getX(), zombie.getY(), zombie.getWidth(), zombie.getHeight());

            // Duyệt qua tất cả Entity (Cây, Đạn...)
            for (Entity entity : entities) {
                // 1. Bỏ qua entity đã chết hoặc không có vị trí
                if (entity.markedForRemoval || !entity.hasComponent(BoundsComponent.class)) {
                    continue;
                }

                // 2. Lấy Bounds của Entity
                BoundsComponent entityBounds = entity.getComponent(BoundsComponent.class);
                
                // 3. Kiểm tra va chạm (Overlap)
                if (Intersector.overlaps(zombieBounds, entityBounds.bounds)) {
                    handleCollision(zombie, entity);
                }
            }
        }
    }

    private void handleCollision(NormalZombie zombie, Entity entity) {
        // --- TRƯỜNG HỢP 1: ZOMBIE GẶP ĐẠN (PROJECTILE) ---
        if (entity instanceof PeaProjectile) {
            // Trừ máu Zombie
            PlantAttackComponent attack = entity.getComponent(PlantAttackComponent.class);
            if (attack != null) {
                zombie.takeDamage(attack.damage);
            }
            
            // Xóa viên đạn ngay lập tức
            entity.markedForRemoval = true;
            return;
        }

        // --- TRƯỜNG HỢP 2: ZOMBIE GẶP CÂY (PLANT) ---
        // Kiểm tra xem entity có phải phe Plant không
        if (entity.hasComponent(TeamComponent.class)) {
            TeamComponent team = entity.getComponent(TeamComponent.class);
            if (team.team != Team.PLANT) return; 

            // A. XỬ LÝ RIÊNG CHO POTATO MINE (MÌN KHOAI TÂY)
            if (entity.hasComponent(ArmingComponent.class)) {
                handlePotatoMine(zombie, entity);
            } 
            // B. XỬ LÝ CÂY THƯỜNG (ZOMBIE ĂN CÂY)
            else {
                // Gọi hàm Zombie ăn cây (Bạn cần implement hàm này bên class NormalZombie)
                // zombie.startEating(entity);
                
                // Tạm thời trừ máu cây để test
                HealthComponent health = entity.getComponent(HealthComponent.class);
                if (health != null) {
                    health.currentHealth -= 1; // Trừ từ từ mỗi frame (giả lập ăn)
                    if (health.isDead()) {
                        entity.markedForRemoval = true;
                        // zombie.stopEating(); // Zombie đi tiếp
                    }
                }
            }
        }
    }

    // Logic đặc biệt cho Potato Mine như bạn yêu cầu
    private void handlePotatoMine(NormalZombie zombie, Entity potatoMine) {
        ArmingComponent arming = potatoMine.getComponent(ArmingComponent.class);

        // CASE 1: Đã trồi lên (Armed) -> BÙM!
        if (arming != null && arming.isArmed) {
            ExplosiveComponent explosive = potatoMine.getComponent(ExplosiveComponent.class);
            if (explosive != null && !explosive.isExploded) {
                // Kích hoạt nổ ngay lập tức (Fuse = 0)
                explosive.fuseTimer = 0; 
                
                // ExplosionSystem sẽ lo phần còn lại (gây damage diện rộng và xóa mìn) ở vòng lặp sau
            }
        } 
        // CASE 2: Chưa trồi lên -> Bị ăn như rau củ thường
        else {
            HealthComponent health = potatoMine.getComponent(HealthComponent.class);
            if (health != null) {
                health.currentHealth -= 2; // Zombie ăn nhanh hơn chút
                if (health.isDead()) {
                    potatoMine.markedForRemoval = true;
                }
            }
        }
    }
}