package pvz.com.entities.projectiles;

import pvz.com.entities.Entity;
import pvz.com.entities.components.*;

public class PeaProjectile extends Entity {

    // startX, startY: Vị trí nòng súng của Peashooter
    // damage: Sát thương viên đạn (lấy từ Peashooter truyền vào)
    public PeaProjectile(float startX, float startY, int damage) {
        super();

        // 1. Vị trí & Kích thước (Đạn nhỏ, khoảng 20x20)
        this.addComponent(new PositionComponent(startX, startY));
        this.addComponent(new SizeComponent(20, 20));
        this.addComponent(new BoundsComponent(startX, startY, 20, 20));

        // 2. Hình ảnh
        this.addComponent(new SpriteComponent("assets/images/Projectiles/pea.png"));

        // 3. Chuyển động (QUAN TRỌNG)
        // Đạn bay thẳng sang phải với tốc độ 400
        this.addComponent(new MovementComponent(400f, 0f));

        // 4. Sát thương
        // Viên đạn mang thông tin sát thương để khi va chạm Zombie sẽ trừ máu
        // Ta tái sử dụng PlantAttackComponent nhưng chỉ dùng trường 'damage'
        // Hoặc tạo một component mới đơn giản hơn là DamageComponent (tùy bạn, ở đây tận dụng cái cũ cho lẹ)
        this.addComponent(new PlantAttackComponent(
            damage, 
            0, null, PlantDamageType.NORMAL, 0 // Mấy tham số sau không quan trọng với đạn
        ));

        // 5. Phe phái (PROJECTILE)
        // CollisionSystem sẽ check: Nếu PROJECTILE va chạm ZOMBIE -> Gây damage và Xóa đạn
        this.addComponent(new TeamComponent(Team.NONE)); // Hoặc Team.PLANT tùy logic va chạm của bạn
        
        // 6. Tự hủy khi bay ra khỏi màn hình
        // Logic này sẽ được xử lý bởi CleanupSystem kiểm tra tọa độ x > màn hình
    }
}
