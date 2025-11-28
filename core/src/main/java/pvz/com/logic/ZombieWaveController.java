package pvz.com.logic;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

import pvz.com.Zombies.NormalZombie;
import pvz.com.managers.GridConfig;

public class ZombieWaveController {

    private static final float MIN_SPAWN_INTERVAL = 2.2f;
    private static final float MAX_SPAWN_INTERVAL = 4.0f;

    private final Array<NormalZombie> zombies = new Array<>();
    private final float worldWidth;
    private final int laneCount;
    private final float startOffsetX;

    private float spawnTimer = 0f;
    private float nextSpawnTime = 0f;
    private int zombiesSpawnedInWave = 0;
    private int maxZombiesInWave;

    public ZombieWaveController(float worldWidth,
            int laneCount,
            float startOffsetX,
            int maxZombiesInWave) {
        this.worldWidth = worldWidth;
        this.laneCount = laneCount;
        this.startOffsetX = startOffsetX;
        this.maxZombiesInWave = maxZombiesInWave;
    }

    public Array<NormalZombie> getZombies() {
        return zombies;
    }

    public void setMaxZombiesInWave(int maxZombiesInWave) {
        this.maxZombiesInWave = maxZombiesInWave;
    }

    public void startWave() {
        spawnTimer = 0f;
        zombiesSpawnedInWave = 0;
        zombies.clear();

        // 2 con demo ban đầu cho người chơi thấy lane
        spawnZombieInLane(0);
        spawnZombieInLane(laneCount - 1);
        zombiesSpawnedInWave = 2;

        scheduleNextSpawn();
    }

    private void scheduleNextSpawn() {
        nextSpawnTime = spawnTimer + MathUtils.random(MIN_SPAWN_INTERVAL, MAX_SPAWN_INTERVAL);
    }

    private void spawnZombieInRandomLane() {
        int laneIndex = MathUtils.random(0, laneCount - 1);
        spawnZombieInLane(laneIndex);
    }

    private void spawnZombieInLane(int laneIndex) {
        float startX = worldWidth + startOffsetX + MathUtils.random(0f, 80f);
        laneIndex = MathUtils.clamp(laneIndex, 0, laneCount - 1);

        NormalZombie z = new NormalZombie();

        // Y giữa ô row = laneIndex
        float laneCenterY = GridConfig.getCellCenterY(laneIndex);

        // Đặt zombie sao cho đứng trên mặt đất (center - nửa chiều cao)
        float zombieY = laneCenterY - z.getHeight() / 2f;

        z.setPosition(startX, zombieY);
        zombies.add(z);
    }

    public void update(float delta) {
        if (zombiesSpawnedInWave < maxZombiesInWave) {
            spawnTimer += delta;

            if (spawnTimer >= nextSpawnTime) {
                spawnZombieInRandomLane();
                zombiesSpawnedInWave++;
                scheduleNextSpawn();
            }
        }

        // update zombie + xoá nếu đi khỏi màn hình
        for (int i = zombies.size - 1; i >= 0; i--) {
            NormalZombie z = zombies.get(i);
            z.act(delta);

            if (z.getX() < -150f) {
                zombies.removeIndex(i);
                // TODO: xử lý khi zombie lọt qua nhà (thua game)
            }
        }
    }

    public void render(SpriteBatch batch) {
        for (NormalZombie z : zombies) {
            z.draw(batch, 1f);
        }
    }
}
