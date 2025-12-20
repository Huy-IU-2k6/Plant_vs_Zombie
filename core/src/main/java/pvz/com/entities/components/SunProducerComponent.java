package pvz.com.entities.components;

public class SunProducerComponent {
    public CooldownComponent cooldown;
    public int sunAmount;

    public SunProducerComponent(float sunCooldown, int sunAmount) {
        this.cooldown = new CooldownComponent(sunCooldown);
        this.sunAmount = sunAmount;
    }
}
