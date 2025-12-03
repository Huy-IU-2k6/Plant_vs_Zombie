package pvz.com.logic;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

import pvz.com.entities.Zombies.Zombies;
import pvz.com.entities.Zombies.NormalZombie;
import pvz.com.entities.Zombies.ConeheadZombie;
import pvz.com.entities.Zombies.BucketheadZombie;
import pvz.com.entities.Zombies.ZombieType;
import pvz.com.managers.GridConfig;

public class ZombieWaveController {

    // ====== CONFIG MÀN CHƠI / SPAWN ======

    // Độ dài 1 màn (từ lúc startWave đến khi coi như "hết màn")
    // Bạn có thể chỉnh tuỳ level: 180f, 240f, 300f...
    private static final float DEFAULT_LEVEL_DURATION = 240f; // 4 phút

    // Spawn interval đầu màn và cuối màn (sẽ nội suy theo tiến độ)
    private static final float START_SPAWN_INTERVAL = 4.0f; // đầu: chậm
    private static final float END_SPAWN_INTERVAL = 2.0f; // cuối: nhanh

    // Độ random quanh interval cơ bản (±20%)
    private static final float INTERVAL_RANDOM_FACTOR = 0.2f;

    // Vị trí X random ngoài màn hình
    private static final float MIN_PRE_SPAWN_OFFSET = 20f;
    private static final float MAX_PRE_SPAWN_OFFSET = 200f;

    // Đường cong số lượng spawn theo thời gian:
    // targetSpawnCount = maxZombiesInWave * (progress^SPAWN_CURVE_POWER)
    // >1 = dồn nhiều về cuối
    private static final float SPAWN_CURVE_POWER = 2.0f;

    // Số zombie demo spawn ngay khi bắt đầu
    private static final int DEMO_ZOMBIE_COUNT = 2;

    // ====== FIELDS ======

    private final Array<Zombies> zombies = new Array<>();
    private final float worldWidth;
    private final int laneCount;
    private final float startOffsetX;

    private int maxZombiesInWave;

    // Thời lượng màn (cho phép tuỳ chỉnh theo level)
    private final float levelDuration;

    // Thời gian đã trôi qua từ lúc startWave
    private float elapsedTime = 0f;

    // Timer dùng cho việc spawn
    private float spawnTimer = 0f;
    private float nextSpawnTime = 0f;

    // Tổng số zombie đã spawn từ đầu màn tới giờ
    private int zombiesSpawnedInWave = 0;

    // ====== CONSTRUCTOR ======

    public ZombieWaveController(float worldWidth,
            int laneCount,
            float startOffsetX,
            int maxZombiesInWave) {
        this(worldWidth, laneCount, startOffsetX, maxZombiesInWave, DEFAULT_LEVEL_DURATION);
    }

    public ZombieWaveController(float worldWidth,
            int laneCount,
            float startOffsetX,
            int maxZombiesInWave,
            float levelDuration) {
        this.worldWidth = worldWidth;
        this.laneCount = laneCount;
        this.startOffsetX = startOffsetX;
        this.maxZombiesInWave = maxZombiesInWave;
        this.levelDuration = levelDuration;
    }

    // ====== PUBLIC API ======

    public Array<Zombies> getZombies() {
        return zombies;
    }

    public void setMaxZombiesInWave(int maxZombiesInWave) {
        this.maxZombiesInWave = maxZombiesInWave;
    }

    /**
     * Gọi khi bắt đầu 1 màn / wave mới.
     */
    public void startWave() {
        elapsedTime = 0f;
        spawnTimer = 0f;
        zombiesSpawnedInWave = 0;
        zombies.clear();

        // 2 con demo ban đầu cho người chơi thấy lane (cho Normal cho dễ)
        spawnZombieInLane(0, ZombieType.NORMAL);
        spawnZombieInLane(laneCount - 1, ZombieType.NORMAL);
        zombiesSpawnedInWave = DEMO_ZOMBIE_COUNT;

        // levelProgress = 0 lúc mới bắt đầu
        scheduleNextSpawn(0f);
    }

    /**
     * Gọi mỗi frame trong GameScreen.
     * delta: Gdx.graphics.getDeltaTime()
     */
    public void update(float delta) {
        // Tính tiến độ màn: 0 -> 1
        elapsedTime += delta;
        float levelProgress = MathUtils.clamp(elapsedTime / levelDuration, 0f, 1f);

        // ====== SPAWN THEO THỜI GIAN ======
        if (zombiesSpawnedInWave < maxZombiesInWave) {
            spawnTimer += delta;

            // Số lượng zombie "nên" được spawn tới thời điểm này
            int targetSpawnCount = getTargetSpawnCount(levelProgress);

            if (zombiesSpawnedInWave < targetSpawnCount &&
                    spawnTimer >= nextSpawnTime) {

                spawnZombieInRandomLane(levelProgress);
                zombiesSpawnedInWave++;
                scheduleNextSpawn(levelProgress);
            }
        }

        // ====== UPDATE & CLEANUP ZOMBIE ======
        for (int i = zombies.size - 1; i >= 0; i--) {
            Zombies z = zombies.get(i);
            z.act(delta);

            // 1) Animation chết xong
            if (z.isDead()) {
                zombies.removeIndex(i);
                continue;
            }

            // 2) Lọt qua bên trái màn hình
            if (z.getX() < -150f) {
                zombies.removeIndex(i);
                // TODO: xử lý khi zombie vào được nhà (thua game)
            }
        }
    }

    public void render(SpriteBatch batch) {
        for (Zombies z : zombies) {
            z.draw(batch, 1f);
        }
    }

    // ====== TÍNH TOÁN THEO TIME ======

    /** Tính interval spawn hiện tại theo tiến độ màn + random nhẹ */
    private float computeSpawnInterval(float levelProgress) {
        float p = MathUtils.clamp(levelProgress, 0f, 1f);

        // Bo cong để về cuối giảm gắt hơn
        p = p * p;

        float baseInterval = MathUtils.lerp(START_SPAWN_INTERVAL, END_SPAWN_INTERVAL, p);

        // Random quanh baseInterval
        float min = baseInterval * (1f - INTERVAL_RANDOM_FACTOR);
        float max = baseInterval * (1f + INTERVAL_RANDOM_FACTOR);
        return MathUtils.random(min, max);
    }

    private void scheduleNextSpawn(float levelProgress) {
        float interval = computeSpawnInterval(levelProgress);
        nextSpawnTime = spawnTimer + interval;
    }

    /** Số zombie "nên" spawn tới thời điểm này (để đầu game ít, cuối game nhiều) */
    private int getTargetSpawnCount(float levelProgress) {
        float p = MathUtils.clamp(levelProgress, 0f, 1f);
        float curve = (float) Math.pow(p, SPAWN_CURVE_POWER);
        return (int) (maxZombiesInWave * curve);
    }

    // ====== CHỌN LOẠI ZOMBIE THEO TIME ======

    private ZombieType pickZombieType(float levelProgress) {
        float p = MathUtils.clamp(levelProgress, 0f, 1f);
        float r = MathUtils.random();

        if (p < 0.25f) {
            // 0–25% màn: chỉ Normal
            return ZombieType.NORMAL;
        } else if (p < 0.7f) {
            // 25–70% màn: Normal + Conehead
            if (r < 0.8f)
                return ZombieType.NORMAL; // 80%
            else
                return ZombieType.CONEHEAD; // 20%
        } else {
            // 70–100% màn: Normal + Conehead + Buckethead
            if (r < 0.6f)
                return ZombieType.NORMAL; // 60%
            else if (r < 0.85f)
                return ZombieType.CONEHEAD; // 25%
            else
                return ZombieType.BUCKETHEAD; // 15%
        }
    }

    // ====== SPAWN ======

    private void spawnZombieInRandomLane(float levelProgress) {
        int laneIndex = MathUtils.random(0, laneCount - 1);
        ZombieType type = pickZombieType(levelProgress);
        spawnZombieInLane(laneIndex, type);
    }

    private void spawnZombieInLane(int laneIndex, ZombieType type) {
        float randomOffset = MathUtils.random(MIN_PRE_SPAWN_OFFSET, MAX_PRE_SPAWN_OFFSET);
        float startX = worldWidth + startOffsetX + randomOffset;
        laneIndex = MathUtils.clamp(laneIndex, 0, laneCount - 1);

        Zombies z;
        switch (type) {
            case NORMAL:
                z = new NormalZombie();
                break;
            case CONEHEAD:
                z = new ConeheadZombie();
                break;
            case BUCKETHEAD:
                z = new BucketheadZombie();
                break;
            default:
                z = new NormalZombie();
        }

        float laneCenterY = GridConfig.getCellCenterY(laneIndex);
        float zombieY = laneCenterY - z.getHeight() / 2f;

        // Nếu sau này muốn buff stats theo time:
        // float progress = MathUtils.clamp(elapsedTime / levelDuration, 0f, 1f);
        // z.applyDifficultyScale(progress); // tuỳ bạn implement

        z.setPosition(startX, zombieY);
        zombies.add(z);
    }
}
