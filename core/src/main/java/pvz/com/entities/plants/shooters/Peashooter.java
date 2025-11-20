package pvz.com.entities.plants.shooters;
import pvz.com.entities.plants.Plant;
import pvz.com.entities.components.*;

public class Peashooter extends Plant {

    public Peashooter(float x, float y) {
        super(x, y, 80, 80); // init position + size + bounds + sprite

        this.health = new HealthComponent(getBaseHealth());          // HP cơ bản
        this.cooldown = new CooldownComponent(getCooldownTime());    // bắn mỗi 1.5s
        this.plantAttack = new PlantAttackComponent(20, 300f, Peashooter.class, PlantDamageType.FIRE, 1.5f);                // damage 20, range 300
        this.sprite = new SpriteComponent("plants/shooters/peashooter.png");
    }

    @Override
    public int getBaseHealth() {
        return 100;
    }

    @Override
    public float getCooldownTime() {
        return 1.5f; // 1.5 giây giữa các lần bắn
    }

    @Override
    public String getTexturePath() {
        return "plants/shooters/peashooter.png";
    }
}

