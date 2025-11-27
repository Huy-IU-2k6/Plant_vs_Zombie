package pvz.com.logic;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

import pvz.com.Zombies.NormalZombie;
import pvz.com.managers.GridConfig;

/**
 * Chịu trách nhiệm spawn zombie theo wave / lane, tách khỏi GameScreen.
 */
public class ZombieWaveController {

    // ===== Cấu hình chung =====
    private static final float MIN_SPAWN_INTERVAL = 2.2f;
    private static final float MAX_SPAWN_INTERVAL = 4.0f;

    private final float worldWidth;
    private final int laneCount;
    private final float startOffsetX;

    // Danh sách zombie thật sự (shared với GameScreen)
    private final Array<NormalZombie> zombies;

    // Trạng thái wave
    private float spawnTimer = 0f;
    private float nextSpawnTime = 0f;
    private int zombiesSpawnedInWave = 0;
    private final int maxZombiesInWave;

    public ZombieWaveController(float worldWidth,
            int laneCount,
            float startOffsetX,
            Array<NormalZombie> zombies,
            int maxZombiesInWave) {
        this.worldWidth = worldWidth;
        this.laneCount = laneCount;
        this.startOffsetX = startOffsetX;
        this.zombies = zombies;
        this.maxZombiesInWave = maxZombiesInWave;
    }

    /** Gọi khi bắt đầu 1 wave mới. */
    public void startWave() {
        spawnTimer = 0f;
        zombiesSpawnedInWave = 0;

        // 2 con demo ban đầu cho người chơi thấy lane
        spawnZombieInLane(0);
        spawnZombieInLane(laneCount - 1);
        zombiesSpawnedInWave = 2;

        scheduleNextSpawn();
    }

    private void scheduleNextSpawn() {
        nextSpawnTime = spawnTimer
                + MathUtils.random(MIN_SPAWN_INTERVAL, MAX_SPAWN_INTERVAL);
    }

    /** Spawn zombie ở lane = rowIndex, dùng GridConfig để lấy Y. */
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

    private void spawnZombieInRandomLane() {
        int laneIndex = MathUtils.random(0, laneCount - 1);
        spawnZombieInLane(laneIndex);
    }

    /** Gọi mỗi frame khi đang PLAYING. */
    public void update(float delta) {
        if (zombiesSpawnedInWave >= maxZombiesInWave) {
            return;
        }

        spawnTimer += delta;

        if (spawnTimer >= nextSpawnTime) {
            spawnZombieInRandomLane();
            zombiesSpawnedInWave++;
            scheduleNextSpawn();
        }
    }
}
