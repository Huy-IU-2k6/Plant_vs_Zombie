package pvz.com.entities.Zombies.strategy;

import pvz.com.entities.Zombies.data.ZombieStats;

public interface DamageStrategy {

    boolean onDamage(ZombieStats stats, int amount);

    boolean hasArmor();
}