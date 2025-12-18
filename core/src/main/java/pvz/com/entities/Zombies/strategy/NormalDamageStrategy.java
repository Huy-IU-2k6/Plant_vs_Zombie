package pvz.com.entities.Zombies.strategy;

import pvz.com.entities.Zombies.data.ZombieStats;

public class NormalDamageStrategy implements DamageStrategy {
    @Override
    public boolean onDamage(ZombieStats stats, int amount) {
        stats.takeDamage(amount);
        return false;
    }
    @Override
    public boolean hasArmor() { return false; }
}