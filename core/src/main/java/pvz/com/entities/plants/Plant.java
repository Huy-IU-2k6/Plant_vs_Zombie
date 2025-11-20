package pvz.com.entities.plants;

import pvz.com.entities.Entity;
import pvz.com.entities.components.*;

// Plant kế thừa Entity để có khả năng chứa Component
public class Plant extends Entity {
    
    // Constructor này tuân thủ SRP: Chỉ lo việc thiết lập các component CƠ BẢN
    public Plant(float x, float y, float width, float height) {
        super(); // Gọi Entity để khởi tạo túi chứa components
        
        this.addComponent(new PositionComponent(x, y));
        this.addComponent(new SizeComponent(width, height));
        this.addComponent(new BoundsComponent(x, y, width, height));
    }
}