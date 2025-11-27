package pvz.com.entities.components;

public class CooldownComponent {
    public float cooldownTime; // thời gian chờ (giây)
    public float timer;        // thời gian đã trôi qua

    public CooldownComponent() {
        this.cooldownTime = 0f;
        this.timer = 0f;
    }

    public CooldownComponent(float cooldownTime) {
        this.cooldownTime = cooldownTime;
        this.timer = 0f;
    }
}
