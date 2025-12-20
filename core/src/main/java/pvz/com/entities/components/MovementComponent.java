package pvz.com.entities.components;

import com.badlogic.gdx.math.Vector2;


public class MovementComponent {


    public Vector2 velocity;


    public float baseSpeed;


    public MovementComponent(float vx, float vy) {
        this.velocity = new Vector2(vx, vy);
        this.baseSpeed = velocity.len();
    }


    public MovementComponent() {
        this(0f, 0f);
    }


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
