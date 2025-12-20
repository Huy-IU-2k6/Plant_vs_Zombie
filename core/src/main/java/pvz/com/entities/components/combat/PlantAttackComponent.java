package pvz.com.entities.components.combat;

import pvz.com.entities.components.types.PlantDamageType;

public class PlantAttackComponent {
    public int damage;
    public float attackSpeed;
    public float timer;
    public float range;
    public Class<?> projectileClass;
    public PlantDamageType damageType;

    public int burstCount;
    public float burstDelay;
    public int shotsFiredInBurst;

    public PlantAttackComponent(int damage, float range, Class<?> projectileClass, PlantDamageType damageType,
            float attackSpeed) {
        this.damage = damage;
        this.range = range;
        this.projectileClass = projectileClass;
        this.damageType = damageType;
        this.attackSpeed = attackSpeed;

        this.timer = 0f;

        this.burstCount = 1;
        this.burstDelay = 0.0f;
        this.shotsFiredInBurst = 0;
    }

    public void setBurstFire(int count, float delay) {
        this.burstCount = count;
        this.burstDelay = delay;
    }
}
