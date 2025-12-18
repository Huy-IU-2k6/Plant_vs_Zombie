package pvz.com.entities.Zombies.data;

public class ZombieStats {
    private float speed;
    private float baseSpeed;
    private int health;

    public ZombieStats(int health, float speed) {
        this.health = health;
        this.baseSpeed = speed;
        this.speed = speed;
    }

    public ZombieStats(ZombieStats other) { // Copy constructor
        this(other.health, other.speed);
    }

    public void takeDamage(int amount) {
        this.health -= amount;
        if (this.health < 0) this.health = 0;
    }

    public boolean isDead() { return health <= 0; }
    public int getHealth() { return health; }
    public float getSpeed() { return speed; }
    public void setSpeed(float speed) { this.speed = speed; }
    public float getBaseSpeed() { return baseSpeed; }
}