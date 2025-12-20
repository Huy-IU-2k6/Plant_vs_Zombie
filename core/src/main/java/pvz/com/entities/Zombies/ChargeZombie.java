package pvz.com.entities.Zombies;

import pvz.com.entities.Zombies.data.ZombieStats;
import pvz.com.entities.Zombies.strategy.NormalDamageStrategy;
import pvz.com.factories.ZombieAssetLoader;

public class ChargeZombie extends BaseZombie {

    private boolean isCharging = true;
    private float chargeTimer = 0f;

    public ChargeZombie() {

        super(new ZombieStats(150, 45f), new NormalDamageStrategy());
    }

    @Override
    protected void loadAnimations() {

        this.currentWalk = ZombieAssetLoader.CHARGE_WALK != null ? ZombieAssetLoader.CHARGE_WALK : ZombieAssetLoader.NORMAL_WALK;
        this.currentEat  = ZombieAssetLoader.CHARGE_EAT != null ? ZombieAssetLoader.CHARGE_EAT : ZombieAssetLoader.NORMAL_EAT;
        this.dieAnim     = ZombieAssetLoader.NORMAL_DIE;
        this.headAnim    = ZombieAssetLoader.HEAD_POP;
        this.charredAnim = ZombieAssetLoader.CHARRED;
    }

    @Override
    public void act(float delta) {
        if (!isDying && !isEating && isCharging) {
            chargeTimer += delta;

            if (chargeTimer > 3f) {
                isCharging = false;

                stats.setSpeed(15f); 
            }
        }
        super.act(delta);
    }
}