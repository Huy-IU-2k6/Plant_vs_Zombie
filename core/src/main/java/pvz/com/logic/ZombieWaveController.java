package pvz.com.logic;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

import pvz.com.entities.Zombies.Zombies;
import pvz.com.entities.Zombies.NormalZombie;
import pvz.com.entities.Zombies.ConeheadZombie;
import pvz.com.entities.Zombies.BucketheadZombie;
import pvz.com.entities.Zombies.ChargeZombie;
import pvz.com.entities.Zombies.ZombieType;
import pvz.com.managers.ScaleManager;
import pvz.com.managers.DesignConfig;

/**
 * Quản lý việc spawn / cập nhật / vẽ các wave zombie.
 *
 * Toàn bộ config vị trí (lane, offset spawn) được hiểu là tọa độ DESIGN
 * (1920x1080),
 * sau đó convert sang world bằng ScaleManager + DesignConfig.
 */
public class ZombieWaveController {

    // ====== CONFIG MÀN CHƠI / SPAWN ======
    private static final float DEFAULT_LEVEL_DURATION = 240f; // 4 phút

    private static final float START_SPAWN_INTERVAL = 4.0f; // đầu: chậm
    private static final float END_SPAWN_INTERVAL = 2.0f; // cuối: nhanh
    private static final float INTERVAL_RANDOM_FACTOR = 0.2f; // ±20%
    private static final float SPAWN_CURVE_POWER = 2.0f; // >1: dồn về cuối

    private static final int DEMO_ZOMBIE_COUNT = 2;

    private static final float MIN_PRE_SPAWN_OFFSET_DESIGN = 20f;
    private static final float MAX_PRE_SPAWN_OFFSET_DESIGN = 200f;

    private static final float LEFT_CULL_MARGIN_DESIGN = 150f;

    // ====== FIELDS ======
    private final Array<Zombies> zombies = new Array<>();

    private final float worldWidth;
    private final float worldHeight;

    private final int laneCount = DesignConfig.ROWS;

    private final float startOffsetXDesign;
    private int maxZombiesInWave;
    private final float levelDuration;

    private float elapsedTime = 0f;
    private float spawnTimer = 0f;
    private float nextSpawnTime = 0f;
    private int zombiesSpawnedInWave = 0;

    // ====== CONSTRUCTOR ======
    public ZombieWaveController(float worldWidth,
            float worldHeight,
            float startOffsetXDesign,
            int maxZombiesInWave) {
        this(worldWidth, worldHeight, startOffsetXDesign, maxZombiesInWave, DEFAULT_LEVEL_DURATION);
    }

    public ZombieWaveController(float worldWidth,
            float worldHeight,
            float startOffsetXDesign,
            int maxZombiesInWave,
            float levelDuration) {
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
        this.startOffsetXDesign = startOffsetXDesign;
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

    /** Wave kết thúc khi: spawn đủ + không còn zombie sống trên màn */
    public boolean isWaveFinished() {
        return zombiesSpawnedInWave >= maxZombiesInWave && zombies.size == 0;
    }

    /** Gọi khi bắt đầu 1 wave mới */
    public void startWave() {
        elapsedTime = 0f;
        spawnTimer = 0f;
        nextSpawnTime = 0f;

        zombiesSpawnedInWave = 0;
        zombies.clear();

        // Spawn demo cho người chơi thấy lane
        int demoSpawned = 0;
        if (laneCount > 0) {
            spawnZombieInLane(0, ZombieType.NORMAL);
            demoSpawned++;
        }
        if (laneCount > 1 && demoSpawned < DEMO_ZOMBIE_COUNT) {
            spawnZombieInLane(laneCount - 1, ZombieType.NORMAL);
            demoSpawned++;
        }

        zombiesSpawnedInWave = demoSpawned;

        // schedule lần spawn tiếp theo (progress = 0)
        scheduleNextSpawn(0f);
    }

    /** Gọi mỗi frame */
    public void update(float delta) {
        elapsedTime += delta;
        float levelProgress = MathUtils.clamp(elapsedTime / levelDuration, 0f, 1f);

        // ===== SPAWN THEO THỜI GIAN =====
        if (zombiesSpawnedInWave < maxZombiesInWave && laneCount > 0) {
            spawnTimer += delta;

            int targetSpawnCount = getTargetSpawnCount(levelProgress);

            if (zombiesSpawnedInWave < targetSpawnCount && spawnTimer >= nextSpawnTime) {
                spawnZombieInRandomLane(levelProgress);
                zombiesSpawnedInWave++;
                scheduleNextSpawn(levelProgress);
            }
        }

        // ===== UPDATE & CLEANUP =====
        float leftCullMarginWorld = ScaleManager.toWorldX(LEFT_CULL_MARGIN_DESIGN, worldWidth);

        for (int i = zombies.size - 1; i >= 0; i--) {
            Zombies z = zombies.get(i);
            if (z == null) {
                zombies.removeIndex(i);
                continue;
            }

            z.act(delta);

            // 1) chết xong animation và đã bị remove khỏi stage
            if (z.isDead() && !z.hasParent()) {
                zombies.removeIndex(i);
                continue;
            }

            // 2) đi lố qua trái -> dọn
            if (z.getX() + z.getWidth() < -leftCullMarginWorld) {
                zombies.removeIndex(i);
            }
        }
    }

    public void render(SpriteBatch batch) {
        for (Zombies z : zombies) {
            if (z != null)
                z.draw(batch, 1f);
        }
    }

    // ====== TÍNH TOÁN SPAWN ======
    private float computeSpawnInterval(float levelProgress) {
        float p = MathUtils.clamp(levelProgress, 0f, 1f);

        // bo cong nhẹ (về cuối nhanh hơn)
        p = p * p;

        float baseInterval = MathUtils.lerp(START_SPAWN_INTERVAL, END_SPAWN_INTERVAL, p);

        float min = baseInterval * (1f - INTERVAL_RANDOM_FACTOR);
        float max = baseInterval * (1f + INTERVAL_RANDOM_FACTOR);
        return MathUtils.random(min, max);
    }

    private void scheduleNextSpawn(float levelProgress) {
        float interval = computeSpawnInterval(levelProgress);
        nextSpawnTime = spawnTimer + interval;
    }

    private int getTargetSpawnCount(float levelProgress) {
        float p = MathUtils.clamp(levelProgress, 0f, 1f);
        float curve = (float) Math.pow(p, SPAWN_CURVE_POWER);
        return (int) (maxZombiesInWave * curve);
    }

    // ====== CHỌN LOẠI ZOMBIE (THÊM CHARGE) ======
    private ZombieType pickZombieType(float levelProgress) {
        float p = MathUtils.clamp(levelProgress, 0f, 1f);
        float r = MathUtils.random();

        if (p < 0.25f) {
            return ZombieType.NORMAL;

        } else if (p < 0.70f) {
            // Normal + Conehead
            return (r < 0.80f) ? ZombieType.NORMAL : ZombieType.CONEHEAD;

        } else {
            // Late game: Normal + Conehead + Buckethead + Charge
            // Tỉ lệ có thể chỉnh tuỳ độ "gắt" mong muốn
            if (r < 0.55f)
                return ZombieType.NORMAL; // 55%
            if (r < 0.78f)
                return ZombieType.CONEHEAD; // 23%
            if (r < 0.92f)
                return ZombieType.BUCKETHEAD; // 14%
            return ZombieType.CHARGE; // 8%
        }
    }

    // ====== SPAWN ======
    private void spawnZombieInRandomLane(float levelProgress) {
        int laneIndex = MathUtils.random(0, laneCount - 1);
        ZombieType type = pickZombieType(levelProgress);
        spawnZombieInLane(laneIndex, type);
    }

    private void spawnZombieInLane(int laneIndex, ZombieType type) {
        laneIndex = MathUtils.clamp(laneIndex, 0, laneCount - 1);

        // spawn x (design -> world)
        float randomOffsetDesign = MathUtils.random(MIN_PRE_SPAWN_OFFSET_DESIGN, MAX_PRE_SPAWN_OFFSET_DESIGN);

        float startOffsetWorld = ScaleManager.toWorldX(startOffsetXDesign, worldWidth);
        float randomOffsetWorld = ScaleManager.toWorldX(randomOffsetDesign, worldWidth);

        float startXWorld = worldWidth + startOffsetWorld + randomOffsetWorld;

        // tạo zombie instance
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
            case CHARGE:
                z = new ChargeZombie();
                break;
            default:
                z = new NormalZombie();
                break;
        }

        // tính y theo lane (design -> world)
        float laneCenterDesignY = DesignConfig.START_Y
                + laneIndex * DesignConfig.CELL_HEIGHT
                + DesignConfig.CELL_HEIGHT / 2f;

        float laneCenterWorldY = ScaleManager.toWorldY(laneCenterDesignY, worldHeight);

        float zombieHeightWorld = z.getHeight();
        float zombieYWorld = laneCenterWorldY - zombieHeightWorld / 2f;

        z.setPosition(startXWorld, zombieYWorld);
        zombies.add(z);
    }
}
