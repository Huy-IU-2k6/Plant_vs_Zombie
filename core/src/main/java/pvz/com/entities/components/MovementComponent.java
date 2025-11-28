package pvz.com.entities.components;
import com.badlogic.gdx.math.Vector2;

public class MovementComponent {
    public Vector2 velocity; // Vận tốc (x, y)
    public float speed;      // Tốc độ di chuyển cơ bản

    public MovementComponent(float speedX, float speedY) {
        this.velocity = new Vector2(speedX, speedY);
        this.speed = Math.abs(speedX); // Lưu tốc độ gốc để dùng khi bị làm chậm
    }
    
    // Constructor mặc định cho tiện
    public MovementComponent() {
        this.velocity = new Vector2(0, 0);
        this.speed = 0;
    }
}