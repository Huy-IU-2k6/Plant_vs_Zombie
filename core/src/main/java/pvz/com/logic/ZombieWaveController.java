package pvz.com.logic;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

import pvz.com.entities.Zombies.Zombies;
import pvz.com.entities.Zombies.NormalZombie;
import pvz.com.entities.Zombies.ConeheadZombie;
import pvz.com.entities.Zombies.BucketheadZombie;
import pvz.com.entities.Zombies.ZombieType;
import pvz.com.managers.ScaleManager;
import pvz.com.managers.DesignConfig;

/**
 * Quản lý việc spawn / cập nhật / vẽ các wave zombie.
 *
 * Toàn bộ config vị trí (lane, offset spawn) được hiểu là tọa độ DESIGN
 * 1920x1080,
 * sau đó convert sang world bằng ScaleManager + DesignConfig.
 */
public class ZombieWaveController {

    // ====== CONFIG MÀN CHƠI / SPAWN ======

    /** Độ dài 1 màn (từ lúc startWave đến khi coi như "hết màn") */
    private static final float DEFAULT_LEVEL_DURATION = 240f; // 4 phút

    /** Spawn interval đầu màn và cuối màn (sẽ nội suy theo tiến độ) */
    private static final float START_SPAWN_INTERVAL = 4.0f; // đầu: chậm
    private static final float END_SPAWN_INTERVAL = 2.0f; // cuối: nhanh

    /** Độ random quanh interval cơ bản (±20%) */
    private static final float INTERVAL_RANDOM_FACTOR = 0.2f;

    /** Đường cong số lượng spawn theo thời gian: >1 = dồn nhiều về cuối */
    private static final float SPAWN_CURVE_POWER = 2.0f;

    /** Số zombie demo spawn ngay khi bắt đầu (ở lane trên cùng / dưới cùng) */
    private static final int DEMO_ZOMBIE_COUNT = 2;

    /** Offset spawn trước mép phải màn hình (đơn vị DESIGN) */
    private static final float MIN_PRE_SPAWN_OFFSET_DESIGN = 20f;
    private static final float MAX_PRE_SPAWN_OFFSET_DESIGN = 200f;

    /** Lề trái để xóa zombie khi đã ra khỏi màn (đơn vị DESIGN) */
    private static final float LEFT_CULL_MARGIN_DESIGN = 150f;

    // ====== FIELDS ======

    private final Array<Zombies> zombies = new Array<>();

    /** Kích thước world (viewport) hiện tại */
    private final float worldWidth;
    private final float worldHeight;

    /** Số lane (thường = DesignConfig.ROWS) */
    private final int laneCount = DesignConfig.ROWS;

    /**
     * Offset thiết kế (design X) tính từ mép phải world đến vị trí spawn gốc.
     * Ví dụ: 0 = spawn ngay sát mép phải, 100 = spawn lệch ra ngoài thêm 100px
     * design.
     */
    private final float startOffsetXDesign;

    /** Số lượng zombie tối đa trong wave này */
    private int maxZombiesInWave;

    /** Thời lượng màn (cho phép tuỳ chỉnh theo level) */
    private final float levelDuration;

    /** Thời gian đã trôi qua từ lúc startWave (0 -> levelDuration) */
    private float elapsedTime = 0f;

    /** Timer dùng cho việc spawn (tích lũy delta) */
    private float spawnTimer = 0f;

    /** Thời điểm (trên spawnTimer) mà sẽ spawn con tiếp theo */
    private float nextSpawnTime = 0f;

    /** Tổng số zombie đã spawn từ đầu wave đến hiện tại (gồm cả con đã chết) */
    private int zombiesSpawnedInWave = 0;

    // ====== CONSTRUCTOR ======

    /**
     * Constructor tiện dụng: số lane mặc định = DesignConfig.ROWS
     */
    public ZombieWaveController(float worldWidth,
            float worldHeight,
            float startOffsetXDesign,
            int maxZombiesInWave) {
        this(worldWidth, worldHeight,
                startOffsetXDesign,
                maxZombiesInWave,
                DEFAULT_LEVEL_DURATION);
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

    /**
     * Wave đã kết thúc chưa? (spawn đủ + không còn zombie sống trên màn)
     */
    public boolean isWaveFinished() {
        return zombiesSpawnedInWave >= maxZombiesInWave && zombies.size == 0;
    }

    /**
     * Gọi khi bắt đầu 1 màn / wave mới.
     */
    public void startWave() {
        elapsedTime = 0f;
        spawnTimer = 0f;
        zombiesSpawnedInWave = 0;
        zombies.clear();

        // 2 con demo ban đầu cho người chơi thấy lane (Normal cho dễ)
        if (laneCount > 0) {
            spawnZombieInLane(0, ZombieType.NORMAL); // lane trên cùng
        }
        if (laneCount > 1) {
            spawnZombieInLane(laneCount - 1, ZombieType.NORMAL); // lane dưới cùng
        }
        zombiesSpawnedInWave = Math.min(DEMO_ZOMBIE_COUNT, laneCount);

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
        float leftCullMarginWorld = ScaleManager.toWorldX(LEFT_CULL_MARGIN_DESIGN, worldWidth);

        for (int i = zombies.size - 1; i >= 0; i--) {
            Zombies z = zombies.get(i);
            z.act(delta);

            // 1) Animation chết xong (Zombies sẽ tự remove() trong act() / deathTimer)
            if (z.isDead() && !z.hasParent()) {
                zombies.removeIndex(i);
                continue;
            }

            // 2) Lọt qua bên trái màn hình khá xa -> dọn
            if (z.getX() + z.getWidth() < -leftCullMarginWorld) {
                zombies.removeIndex(i);
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
        // đánh dấu thời điểm (trên trục spawnTimer) cho lần spawn kế tiếp
        nextSpawnTime = spawnTimer + interval;
    }

    /**
     * Số zombie "nên" spawn tới thời điểm này (để đầu game ít, cuối game nhiều).
     * Dựa trên hàm p^SPAWN_CURVE_POWER.
     */
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
        // Clamp lane
        laneIndex = MathUtils.clamp(laneIndex, 0, laneCount - 1);

        // Offset spawn (DESIGN) -> WORLD
        float randomOffsetDesign = MathUtils.random(
                MIN_PRE_SPAWN_OFFSET_DESIGN,
                MAX_PRE_SPAWN_OFFSET_DESIGN);

        float startOffsetWorld = ScaleManager.toWorldX(startOffsetXDesign, worldWidth);
        float randomOffsetWorld = ScaleManager.toWorldX(randomOffsetDesign, worldWidth);

        float startXWorld = worldWidth + startOffsetWorld + randomOffsetWorld;

        // Tạo instance zombie theo type
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

        // ===== TÍNH Y THEO DESIGNCONFIG (DESIGN -> WORLD) =====
        // Tâm ô: START_Y + row * CELL_HEIGHT + CELL_HEIGHT / 2
        float laneCenterDesignY = DesignConfig.START_Y
                + laneIndex * DesignConfig.CELL_HEIGHT
                + DesignConfig.CELL_HEIGHT / 2f;

        float laneCenterWorldY = ScaleManager.toWorldY(laneCenterDesignY, worldHeight);

        // Chiều cao zombie đã scale theo world (constructor zombie tự set)
        float zombieHeightWorld = z.getHeight();
        float zombieYWorld = laneCenterWorldY - zombieHeightWorld / 2f;

        z.setPosition(startXWorld, zombieYWorld);
        zombies.add(z);
    }
}
