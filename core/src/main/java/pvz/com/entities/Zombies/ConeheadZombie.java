package pvz.com.entities.Zombies;

import pvz.com.entities.Zombies.data.ZombieStats;
import pvz.com.entities.Zombies.strategy.ArmorDamageStrategy;
import pvz.com.factories.ZombieAssetLoader;

public class ConeheadZombie extends BaseZombie {

    public ConeheadZombie() {
        // Máu 200, Giáp nón 370
        super(new ZombieStats(200, 15f), new ArmorDamageStrategy(370));
    }

    @Override
    protected void loadAnimations() {
        this.currentWalk = ZombieAssetLoader.CONE_WALK;
        this.currentEat  = ZombieAssetLoader.CONE_EAT;
        
        // Anim chết dùng chung
        this.dieAnim     = ZombieAssetLoader.NORMAL_DIE;
        this.headAnim    = ZombieAssetLoader.HEAD_POP;
        this.charredAnim = ZombieAssetLoader.CHARRED;
    }

    @Override
    protected void onArmorBroken() {
        // Nón vỡ -> thành Normal
        this.currentWalk = ZombieAssetLoader.NORMAL_WALK;
        this.currentEat  = ZombieAssetLoader.NORMAL_EAT;
    }
}