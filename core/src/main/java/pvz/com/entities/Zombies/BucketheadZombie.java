package pvz.com.entities.Zombies;

import pvz.com.entities.Zombies.data.ZombieStats;
import pvz.com.entities.Zombies.strategy.ArmorDamageStrategy;
import pvz.com.factories.ZombieAssetLoader;

public class BucketheadZombie extends BaseZombie {

    public BucketheadZombie() {

        super(new ZombieStats(200, 15f), new ArmorDamageStrategy(1100));
    }

    @Override
    protected void loadAnimations() {

        this.currentWalk = ZombieAssetLoader.BUCKET_WALK != null ? ZombieAssetLoader.BUCKET_WALK : ZombieAssetLoader.CONE_WALK;
        this.currentEat  = ZombieAssetLoader.BUCKET_EAT != null ? ZombieAssetLoader.BUCKET_EAT : ZombieAssetLoader.CONE_EAT;
        
        this.dieAnim     = ZombieAssetLoader.NORMAL_DIE;
        this.headAnim    = ZombieAssetLoader.HEAD_POP;
        this.charredAnim = ZombieAssetLoader.CHARRED;
    }

    @Override
    protected void onArmorBroken() {
        this.currentWalk = ZombieAssetLoader.NORMAL_WALK;
        this.currentEat  = ZombieAssetLoader.NORMAL_EAT;
    }
}