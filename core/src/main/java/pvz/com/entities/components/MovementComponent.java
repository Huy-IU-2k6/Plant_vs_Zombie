package pvz.com.entities.components;
import com.badlogic.gdx.math.Vector2;

import com.badlogic.gdx.math.Vector2;

// Nếu bạn có interface Component thì thêm: implements Component
public class MovementComponent {

    // Vector vận tốc chính
    public Vector2 velocity;

    // Tốc độ gốc (độ lớn), dùng cho slow/freeze...
    public float baseSpeed;

    // Constructor đầy đủ: truyền vx, vy
    public MovementComponent(float vx, float vy) {
        this.velocity = new Vector2(vx, vy);
        this.baseSpeed = velocity.len(); // độ lớn vector (tốc độ)
    }

    // Constructor mặc định
    public MovementComponent() {
        this(0f, 0f);
    }

    // Helper cho code cũ nếu bạn đang xài .vx, .vy trong MovementSystem:
    public float getVx() {
        return velocity.x;
    }

    public float getVy() {
        return velocity.y;
    }

    public void setVx(float vx) {
        this.velocity.x = vx;
        this.baseSpeed = velocity.len();
    }

    public void setVy(float vy) {
        this.velocity.y = vy;
        this.baseSpeed = velocity.len();
    }
}
