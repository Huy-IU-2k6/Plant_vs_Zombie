package pvz.com.entities.Zombies;

import pvz.com.entities.Zombies.data.ZombieStats;
import pvz.com.entities.Zombies.strategy.NormalDamageStrategy;
import pvz.com.factories.ZombieAssetLoader;

public class NormalZombie extends BaseZombie {

    public NormalZombie() {
        super(new ZombieStats(100, 15f), new NormalDamageStrategy());
    }

    @Override
    protected void loadAnimations() {

        this.currentWalk = ZombieAssetLoader.NORMAL_WALK;
        this.currentEat  = ZombieAssetLoader.NORMAL_EAT;
        this.dieAnim     = ZombieAssetLoader.NORMAL_DIE;
        this.headAnim    = ZombieAssetLoader.HEAD_POP;
        this.charredAnim = ZombieAssetLoader.CHARRED;
        if (this.currentWalk == null) System.err.println("ERROR: NormalZombie Walk Anim is NULL!");
    }
}