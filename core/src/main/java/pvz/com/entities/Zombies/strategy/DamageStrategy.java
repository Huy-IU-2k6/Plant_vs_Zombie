package pvz.com.entities.Zombies.strategy;

import pvz.com.entities.Zombies.data.ZombieStats;

public interface DamageStrategy {
    // Trả về true nếu giáp vừa bị vỡ
    boolean onDamage(ZombieStats stats, int amount);
    // Để biết đang còn giáp hay không (để vẽ hình to/nhỏ)
    boolean hasArmor();
}