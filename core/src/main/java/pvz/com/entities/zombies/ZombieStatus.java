package pvz.com.entities.zombies;

public class ZombieStatus {
    private float speed;
    private float baseSpeed;
    private int health;

    public ZombieStatus(int health, float speed) {
        this.health = health;
        this.baseSpeed = speed;
        this.speed = speed;
    }

    public ZombieStatus(ZombieStatus other) {
        this(other.health, other.speed);
    }

    public void takeDamage(int amount) {
        this.health -= amount;
        if (this.health < 0)
            this.health = 0;
    }

    public boolean isDead() {
        return health <= 0;
    }

    public int getHealth() {
        return health;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public float getBaseSpeed() {
        return baseSpeed;
    }
}
