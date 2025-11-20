package pvz.com.entities.plants;

import pvz.com.entities.Entity;
import pvz.com.entities.components.*;

public abstract class Plant extends Entity {
        public PlantAttackComponent plantAttack;
        public Plant(float x, float y, float w, float h) {
            super(); // gọi constructor protected

            this.position = new PositionComponent(x, y);
            this.size = new SizeComponent(w, h);
            this.bounds = new BoundsComponent(x, y, w, h);

            this.health = new HealthComponent(getBaseHealth());
            this.cooldown = new CooldownComponent(getCooldownTime());
            this.sprite = new SpriteComponent(getTexturePath());
        }
    public abstract int getBaseHealth();          // máu tối đa
    public abstract float getCooldownTime();      // thời gian hồi chiêu
    public abstract String getTexturePath();
}


