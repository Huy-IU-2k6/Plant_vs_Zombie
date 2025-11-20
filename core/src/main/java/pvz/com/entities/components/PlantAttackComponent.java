package pvz.com.entities.components;

// PlantAttackComponent.java
public class PlantAttackComponent {
    public int damage;
    public float range;
    public Class<?> projectileType;
    public PlantDamageType damageType;
    public CooldownComponent cooldown;

    public PlantAttackComponent(int damage, float range, Class<?> projectileType, PlantDamageType damageType, float cooldownTime) {
        this.damage = damage;
        this.range = range;
        this.projectileType = projectileType;
        this.damageType = damageType;
        this.cooldown = new CooldownComponent(cooldownTime);
    }
}

