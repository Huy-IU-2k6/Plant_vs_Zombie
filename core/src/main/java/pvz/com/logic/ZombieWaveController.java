package pvz.com.logic;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

import pvz.com.entities.zombies.*;
import pvz.com.managers.ScaleManager;
import pvz.com.managers.DesignConfig;

public class ZombieWaveController {

    private static final float DEFAULT_LEVEL_DURATION = 240f;

    private static final float START_SPAWN_INTERVAL = 4.0f;
    private static final float END_SPAWN_INTERVAL = 1.0f;
    private static final float INTERVAL_RANDOM_FACTOR = 0.2f;
    private static final float SPAWN_CURVE_POWER = 2.0f;

    private static final int DEMO_ZOMBIE_COUNT = 2;

    private static final float MIN_PRE_SPAWN_OFFSET_DESIGN = 20f;
    private static final float MAX_PRE_SPAWN_OFFSET_DESIGN = 200f;

    private static final float LEFT_CULL_MARGIN_DESIGN = 150f;

    private final Array<BaseZombie> zombies = new Array<>();

    private final float worldWidth;
    private final float worldHeight;

    private final int laneCount = DesignConfig.ROWS;

    private final float startOffsetXDesign;
    private int maxzombiesInWave;
    private final float levelDuration;

    private float elapsedTime = 0f;
    private float spawnTimer = 0f;
    private float nextSpawnTime = 0f;
    private int zombiesSpawnedInWave = 0;

    private final GameState gameState;
    private boolean waveStarted = false;
    private boolean triggeredWin = false;

    public ZombieWaveController(float worldWidth,
            float worldHeight,
            float startOffsetXDesign,
            int maxzombiesInWave) {
        this(worldWidth, worldHeight, startOffsetXDesign, maxzombiesInWave, DEFAULT_LEVEL_DURATION, null);
    }

    public ZombieWaveController(float worldWidth,
            float worldHeight,
            float startOffsetXDesign,
            int maxzombiesInWave,
            float levelDuration) {
        this(worldWidth, worldHeight, startOffsetXDesign, maxzombiesInWave, levelDuration, null);
    }

    public ZombieWaveController(float worldWidth,
            float worldHeight,
            float startOffsetXDesign,
            int maxzombiesInWave,
            float levelDuration,
            GameState gameState) {
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
        this.startOffsetXDesign = startOffsetXDesign;
        this.maxzombiesInWave = maxzombiesInWave;
        this.levelDuration = levelDuration;
        this.gameState = gameState;
    }

    public Array<BaseZombie> getzombies() { return zombies; }

    public void setMaxzombiesInWave(int maxzombiesInWave) {
        this.maxzombiesInWave = maxzombiesInWave;
    }

    public boolean isWaveFinished() {
        return zombiesSpawnedInWave >= maxzombiesInWave && zombies.size == 0;
    }

    public void startWave() {
        elapsedTime = 0f;
        spawnTimer = 0f;
        nextSpawnTime = 0f;

        zombiesSpawnedInWave = 0;
        zombies.clear();

        waveStarted = true;
        triggeredWin = false;

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
        scheduleNextSpawn(0f);
    }

    public void update(float delta) {
        if (!waveStarted)
            return;
        if (gameState != null && gameState.isGameOver())
            return;

        elapsedTime += delta;
        float levelProgress = MathUtils.clamp(elapsedTime / levelDuration, 0f, 1f);

        if (zombiesSpawnedInWave < maxzombiesInWave && laneCount > 0) {
            spawnTimer += delta;

            int targetSpawnCount = getTargetSpawnCount(levelProgress);

            if (zombiesSpawnedInWave < targetSpawnCount && spawnTimer >= nextSpawnTime) {
                spawnZombieInRandomLane(levelProgress);
                zombiesSpawnedInWave++;
                scheduleNextSpawn(levelProgress);
            }
        }

        float leftCullMarginWorld = ScaleManager.toWorldX(LEFT_CULL_MARGIN_DESIGN, worldWidth);

        for (int i = zombies.size - 1; i >= 0; i--) {
            BaseZombie z = zombies.get(i);
            if (z == null) { zombies.removeIndex(i); continue; }

            z.act(delta);

            if (z.isDead() && !z.hasParent()) {
                zombies.removeIndex(i);
                continue;
            }

            if (z.getX() + z.getWidth() < -leftCullMarginWorld) {
                zombies.removeIndex(i);
            }
        }

        if (!triggeredWin && isWaveFinished()) {
            triggeredWin = true;
            if (gameState != null)
                gameState.setGameOver(true);
        }
    }

    public void render(SpriteBatch batch) {
        for (BaseZombie z : zombies) {
            if (z != null) z.draw(batch, 1f);
        }
    }

    private float computeSpawnInterval(float levelProgress) {
        float p = MathUtils.clamp(levelProgress, 0f, 1f);
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
        return (int) (maxzombiesInWave * curve);
    }

    private ZombieType pickZombieType(float levelProgress) {
        float p = MathUtils.clamp(levelProgress, 0f, 1f);
        float r = MathUtils.random();

        if (p < 0.25f)
            return ZombieType.NORMAL;

        if (p < 0.70f) {
            return (r < 0.80f) ? ZombieType.NORMAL : ZombieType.CONEHEAD;
        }

        if (r < 0.55f)
            return ZombieType.NORMAL;
        if (r < 0.78f)
            return ZombieType.CONEHEAD;
        if (r < 0.92f)
            return ZombieType.BUCKETHEAD;
        return ZombieType.CHARGE;
    }

    private void spawnZombieInRandomLane(float levelProgress) {
        int laneIndex = MathUtils.random(0, laneCount - 1);
        ZombieType type = pickZombieType(levelProgress);
        spawnZombieInLane(laneIndex, type);
    }

    private void spawnZombieInLane(int laneIndex, ZombieType type) {
        laneIndex = MathUtils.clamp(laneIndex, 0, laneCount - 1);

        float randomOffsetDesign = MathUtils.random(MIN_PRE_SPAWN_OFFSET_DESIGN, MAX_PRE_SPAWN_OFFSET_DESIGN);

        float startOffsetWorld = ScaleManager.toWorldX(startOffsetXDesign, worldWidth);
        float randomOffsetWorld = ScaleManager.toWorldX(randomOffsetDesign, worldWidth);

        float startXWorld = worldWidth + startOffsetWorld + randomOffsetWorld;

        BaseZombie z;
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
