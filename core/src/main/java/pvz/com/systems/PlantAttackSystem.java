package pvz.com.systems;

import java.util.List;
import pvz.com.entities.Zombies.BaseZombie;
import pvz.com.entities.components.HealthComponent;
import pvz.com.entities.components.PlantAttackComponent;
import pvz.com.entities.components.PositionComponent;
import pvz.com.entities.plants.Plant;
import pvz.com.logic.ZombieWaveController;
import pvz.com.managers.DesignConfig;

public class PlantAttackSystem {

    private static final float LANE_Y_TOLERANCE = 50f;
    private static final float PROJECTILE_SPAWN_OFFSET_X = 20f;
    private static final float PROJECTILE_SPAWN_OFFSET_Y = 50f;
    private static final float ZOMBIE_ENTER_SCREEN_MARGIN = 120f;
    private static final float COOLDOWN_MULTIPLIER = 3.2f;

    private final IGameSpawner spawner;
    private final ZombieWaveController zombieController;

    public PlantAttackSystem(IGameSpawner spawner, ZombieWaveController zombieController) {
        this.spawner = spawner;
        this.zombieController = zombieController;
    }

    public void update(List<Plant> plants, float deltaTime) {
        if (plants == null || spawner == null || zombieController == null)
            return;

        for (Plant plant : plants) {
            if (plant == null)
                continue;


            HealthComponent hp = plant.getComponent(HealthComponent.class);
            if (hp != null && hp.currentHealth <= 0)
                continue;

            PlantAttackComponent atk = plant.getComponent(PlantAttackComponent.class);
            PositionComponent pos = plant.getComponent(PositionComponent.class);
            if (atk == null || pos == null)
                continue;


            if (atk.burstCount <= 0)
                atk.burstCount = 1;

            atk.timer += deltaTime;


            if (atk.shotsFiredInBurst > 0 && atk.shotsFiredInBurst < atk.burstCount) {
                if (!shouldShoot(pos, atk.range)) {
                    atk.shotsFiredInBurst = 0;
                    atk.timer = getEffectiveCooldown(atk);
                    continue;
                }

                if (atk.timer >= atk.burstDelay) {
                    fire(pos, atk);
                    atk.shotsFiredInBurst++;
                    atk.timer = 0f;
                    if (atk.shotsFiredInBurst >= atk.burstCount) {
                        atk.shotsFiredInBurst = 0;
                    }
                }
                continue;
            }


            float cd = getEffectiveCooldown(atk);
            if (atk.timer < cd)
                continue;

            if (shouldShoot(pos, atk.range)) {
                fire(pos, atk);
                atk.shotsFiredInBurst = 1;

                if (atk.burstCount <= 1) {
                    atk.shotsFiredInBurst = 0;
                    atk.timer = 0f;
                } else {
                    atk.timer = 0f;
                }
            } else {
                atk.timer = cd;
                atk.shotsFiredInBurst = 0;
            }
        }
    }

    private float getEffectiveCooldown(PlantAttackComponent atk) {
        return atk.attackSpeed * COOLDOWN_MULTIPLIER;
    }

    private void fire(PositionComponent pos, PlantAttackComponent atk) {
        spawner.spawnProjectile(
                pos.x + PROJECTILE_SPAWN_OFFSET_X,
                pos.y + PROJECTILE_SPAWN_OFFSET_Y,
                atk.damage,
                atk.damageType,
                atk.projectileClass);
    }

    private boolean shouldShoot(PositionComponent plantPos, float range) {
        if (zombieController == null || zombieController.getZombies() == null)
            return false;

        float screenRight = DesignConfig.BASE_SCREEN_W;


        for (BaseZombie z : zombieController.getZombies()) {
            if (z == null)
                continue;



            if (z.isDead())
                continue;


            if (z.getX() > (screenRight - ZOMBIE_ENTER_SCREEN_MARGIN))
                continue;


            if (Math.abs(z.getY() - plantPos.y) > LANE_Y_TOLERANCE)
                continue;


            float dx = z.getX() - plantPos.x;
            if (dx > 0f && dx <= range)
                return true;
        }
        return false;
    }
}
