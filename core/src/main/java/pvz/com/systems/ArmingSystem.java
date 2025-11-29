package pvz.com.systems;

import com.badlogic.gdx.graphics.Texture;
import pvz.com.entities.Entity;
import pvz.com.entities.components.*;

import java.util.List;

public class ArmingSystem {
    private List<Entity> entities;

    public ArmingSystem(List<Entity> entities) {
        this.entities = entities;
    }

    public void update(float deltaTime) {
        for (Entity entity : entities) {
            // Chỉ xử lý những cây có khả năng Arming (như Potato Mine)
            if (entity.hasComponent(ArmingComponent.class)) {
                processArming(entity, deltaTime);
            }
        }
    }

    private void processArming(Entity entity, float deltaTime) {
        ArmingComponent arming = entity.getComponent(ArmingComponent.class);

        // Nếu đã armed rồi thì thôi, không làm gì nữa
        if (arming.isArmed) return;

        // Đếm ngược
        arming.armingTimer -= deltaTime;

        // Khi hết giờ -> Biến hình!
        if (arming.armingTimer <= 0) {
            arming.isArmed = true;

            // 1. Đổi hình ảnh sang dạng "Sẵn sàng" (Củ khoai thò đầu lên)
            if (entity.hasComponent(SpriteComponent.class)) {
                SpriteComponent sprite = entity.getComponent(SpriteComponent.class);
                // Lưu ý: Nên dùng AssetManager để lấy texture thay vì new Texture liên tục (gây lag)
                // Ở đây mình viết new Texture cho dễ hiểu
                sprite.sprite.setTexture(new Texture("images/Plants/potatomine_ready.gif"));
            }
            
            // 2. Play âm thanh "Sproing!" nếu muốn
            System.out.println("POTATO MINE IS READY!");
        }
    }
}
