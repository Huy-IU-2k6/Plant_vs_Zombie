package pvz.com.entities.zombies.strategy;

import pvz.com.entities.zombies.ZombieStatus;

public class NormalDamageStrategy implements DamageStrategy {
    @Override
    public boolean onDamage(ZombieStatus stats, int amount) {
        stats.takeDamage(amount);
        return false;
    }
    @Override
    public boolean hasArmor() { return false; }
}
