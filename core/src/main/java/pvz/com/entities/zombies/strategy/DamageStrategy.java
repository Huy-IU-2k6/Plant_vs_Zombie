package pvz.com.entities.zombies.strategy;

import pvz.com.entities.zombies.ZombieStatus;

public interface DamageStrategy {

    boolean onDamage(ZombieStatus stats, int amount);

    boolean hasArmor();
}
