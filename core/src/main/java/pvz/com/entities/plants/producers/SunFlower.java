package pvz.com.entities.plants.producers;

import pvz.com.entities.plants.Plant;
import pvz.com.entities.components.*;

public class SunFlower extends Plant {

    public SunFlower(float x, float y) {
        super(x


            , y, 80, 80); // init position, size, bounds, sprite

        this.health = new HealthComponent(getBaseHealth());
        this.sprite = new SpriteComponent(getTexturePath());

        // SunProducerComponent riêng, có CooldownComponent riêng
       SunProducerComponent sunProducer = new SunProducerComponent(getCooldownTime(), getSunAmount());
    }

    @Override
    public int getBaseHealth() {
        return 100;
    }

    @Override
    public float getCooldownTime() {
        return 7f; // spawn Sun mỗi 7 giây
    }

    public int getSunAmount() {
        return 25; // mỗi lần spawn 25 Sun
    }

    @Override
    public String getTexturePath() {
        return "plants/producers/sunflower.png";
    }
}
