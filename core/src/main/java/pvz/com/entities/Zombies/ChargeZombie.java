package pvz.com.entities.Zombies;

import pvz.com.entities.Zombies.data.ZombieStats;
import pvz.com.entities.Zombies.strategy.NormalDamageStrategy;
import pvz.com.factories.ZombieAssetLoader;

public class ChargeZombie extends BaseZombie {

    private boolean isCharging = true;
    private float chargeTimer = 0f;

    public ChargeZombie() {
        // Tốc độ gốc cao (45f)
        super(new ZombieStats(150, 45f), new NormalDamageStrategy());
    }

    @Override
    protected void loadAnimations() {
        // Fallback về Normal nếu chưa có ảnh Charge
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
            // Chạy nhanh trong 3 giây đầu
            if (chargeTimer > 3f) {
                isCharging = false;
                // Giảm tốc về 15f (bằng Normal)
                stats.setSpeed(15f); 
            }
        }
        super.act(delta);
    }
}