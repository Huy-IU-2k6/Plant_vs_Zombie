package pvz.com.entities.zombies.strategy;

import pvz.com.entities.zombies.ZombieStatus;

public class ArmorDamageStrategy implements DamageStrategy {
    private int armorHealth;

    public ArmorDamageStrategy(int armorHealth) {
        this.armorHealth = armorHealth;
    }

    @Override
    public boolean onDamage(ZombieStatus stats, int amount) {
        if (armorHealth > 0) {
            armorHealth -= amount;
            if (armorHealth <= 0) {

                int overflow = -armorHealth;
                stats.takeDamage(overflow);
                armorHealth = 0;
                return true;
            }
        } else {
            stats.takeDamage(amount);
        }
        return false;
    }

    @Override
    public boolean hasArmor() { return armorHealth > 0; }
}
