package pvz.com.entities.components;

public class SunPickupComponent {
    public int amount;
    public float lifeTime;
    public float aliveTime;

    public SunPickupComponent(int amount, float lifeTime) {
        this.amount = amount;
        this.lifeTime = lifeTime;
        this.aliveTime = 0f;
    }
}
