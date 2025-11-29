package pvz.com.entities.components;

public class SunPickupComponent {
    public int amount; // +bao nhiêu sun khi nhặt
    public float lifeTime; // tồn tại bao lâu (giây)
    public float aliveTime; // đã sống được bao lâu

    public SunPickupComponent(int amount, float lifeTime) {
        this.amount = amount;
        this.lifeTime = lifeTime;
        this.aliveTime = 0f;
    }
}
