package pvz.com.entities.systems;

import pvz.com.entities.components.PlantDamageType;

public interface IGameSpawner {
    void spawnSun(float x, float y, int amount);

    void spawnProjectile(float x, float y, int damage, PlantDamageType type, Class<?> projectileClass);
}
