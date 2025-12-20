package pvz.com.entities.components;

public class CooldownComponent {
    public float cooldownTime;
    public float timer;

    public CooldownComponent() {
        this.cooldownTime = 0f;
        this.timer = 0f;
    }

    public CooldownComponent(float cooldownTime) {
        this.cooldownTime = cooldownTime;
        this.timer = 0f;
    }
}
