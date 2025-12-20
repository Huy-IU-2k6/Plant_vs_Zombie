package pvz.com.entities.zombies;

import pvz.com.entities.zombies.strategy.ArmorDamageStrategy;
import pvz.com.entities.factories.ZombieAssetLoader;

public class ConeheadZombie extends BaseZombie {

    public ConeheadZombie() {

        super(new ZombieStatus(200, 15f), new ArmorDamageStrategy(370));
    }

    @Override
    protected void loadAnimations() {
        this.currentWalk = ZombieAssetLoader.CONE_WALK;
        this.currentEat = ZombieAssetLoader.CONE_EAT;

        this.dieAnim = ZombieAssetLoader.NORMAL_DIE;
        this.headAnim = ZombieAssetLoader.HEAD_POP;
        this.charredAnim = ZombieAssetLoader.CHARRED;
    }

    @Override
    protected void onArmorBroken() {

        this.currentWalk = ZombieAssetLoader.NORMAL_WALK;
        this.currentEat = ZombieAssetLoader.NORMAL_EAT;
    }
}
