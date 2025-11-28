package pvz.com.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
<<<<<<< HEAD
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
=======
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.math.MathUtils;
>>>>>>> origin

import pvz.com.Zombies.NormalZombie;
import pvz.com.managers.FontManager;
import pvz.com.managers.BackgroundManager;
import pvz.com.ui.CountdownActor;
import pvz.com.items.ItemType;
import pvz.com.items.PlantCard;
import pvz.com.items.SeedBank;
import pvz.com.items.LawnMower;

// ECS imports
import pvz.com.entities.Entity;
import pvz.com.entities.plants.Plant;
import pvz.com.entities.components.PlantDamageType;
import pvz.com.entities.projectiles.PeaProjectile;
import pvz.com.factories.PlantFactory;
import pvz.com.systems.IGameSpawner;
import pvz.com.systems.RenderSystem;
import pvz.com.systems.SunProductionSystem;
import pvz.com.systems.PlantAttackSystem;

import java.util.ArrayList;
import java.util.List;

public class GameScreen implements Screen, IGameSpawner {

    // ===== World & layout =====
    private static final float WORLD_WIDTH = 800f;
    private static final float WORLD_HEIGHT = 600f;

<<<<<<< HEAD
    private final Game game;
    private final SpriteBatch batch;
    private final Stage hudStage;
=======
    // ===== Game config =====
    private static final float COUNTDOWN_DURATION = 6f;
    private static final int INITIAL_SUN = 150;

    // ===== Zombie lane config =====
    private static final int ZOMBIE_LANE_COUNT = 5;
    private static final float ZOMBIE_START_OFFSET_X = 50f;
    private static final float ZOMBIE_FIRST_LANE_Y = 100f;
    private static final float ZOMBIE_LANE_GAP_Y = 100f;
>>>>>>> origin

    // ===== SeedBank layout =====
    private static final float SEED_BANK_HEIGHT = 110f;
    private static final float SEED_BANK_MARGIN_TOP = 20f;
    private static final float SEED_BANK_MARGIN_LEFT = 50f;

<<<<<<< HEAD
    // Background + Countdown (HEAD branch)
    private final BackgroundManager backgroundManager;
    private CountdownActor countdown;

=======
>>>>>>> origin
    private enum State {
        COUNTDOWN,
        PLAYING
    }
<<<<<<< HEAD
    private State state = State.COUNTDOWN;
    private float countdownTime = 6f;

    // ECS
    private Texture bgTex;
    private List<Entity> entities;
    private List<Plant> plants;
    private RenderSystem renderSystem;
    private SunProductionSystem sunSystem;
    private PlantAttackSystem attackSystem;
=======

    // ===== Core refs =====
    private final Game game;
    private final SpriteBatch batch;
    private final Stage hudStage;
    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final BackgroundManager backgroundManager;

    // ===== UI & state =====
    private CountdownActor countdown;
    private final Array<PlantCard> plantCards = new Array<>();
    private final SeedBank seedBank;
    private final BitmapFont hudFont;
    private State state = State.COUNTDOWN;

    // ===== Sun HUD =====
    private int sunPoints = INITIAL_SUN;

    // ===== Entities =====
    private final Array<NormalZombie> zombies = new Array<>();
    private final Array<LawnMower> lawnMowers = new Array<>();

    // ===== Zombie spawn control =====
    private float spawnTimer = 0f;
    private float nextSpawnTime = 0f;
    private int zombiesSpawnedInWave = 0;
    private int maxZombiesInWave = 20;

    private static final float MIN_SPAWN_INTERVAL = 2.2f;
    private static final float MAX_SPAWN_INTERVAL = 4.0f;

    private final float[] laneYs = new float[ZOMBIE_LANE_COUNT];
>>>>>>> origin

    public GameScreen(Game game) {
        this.game = game;
        this.batch = new SpriteBatch();
        this.hudStage = new Stage(new ScreenViewport());

<<<<<<< HEAD
        // Camera + Viewport
=======
        batch = new SpriteBatch();

        // HUD stage (UI)
        hudStage = new Stage(new ScreenViewport());
        hudStage.getRoot().setUserObject(this);

        // Camera + viewport world 800x600
>>>>>>> origin
        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        camera.position.set(WORLD_WIDTH / 2f, WORLD_HEIGHT / 2f, 0f);
        camera.update();

<<<<<<< HEAD
        // Background Manager
        backgroundManager = new BackgroundManager();

        // Countdown Actor
        countdown = new CountdownActor(countdownTime, FontManager.getPvzFont());
        countdown.setPosition(400f, 500f);
        hudStage.addActor(countdown);

        // ECS init
        entities = new ArrayList<>();
        plants = new ArrayList<>();

        try {
            bgTex = new Texture(Gdx.files.internal("assets/images/backgrounds/Lawn.jpeg"));
            bgTex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        } catch (Exception e) {
            Gdx.app.error("GameScreen", "Lỗi load BG", e);
        }

        renderSystem = new RenderSystem(batch);
        sunSystem = new SunProductionSystem(this);
        attackSystem = new PlantAttackSystem(this);

        // Test plants
        spawnPlant(PlantFactory.createSunflower(100, 200));
        spawnPlant(PlantFactory.createPeashooter(200, 200));
        spawnPlant(PlantFactory.createWallnut(300, 300));
    }

    private void spawnPlant(Plant plant) {
        entities.add(plant);
        plants.add(plant);
=======
        // Background
        backgroundManager = new BackgroundManager();

        // lane Y
        initLanes();

        // SeedBank
        seedBank = new SeedBank();
        layoutSeedBank();
        seedBank.setVisible(false);
        hudStage.addActor(seedBank);

        // Countdown
        countdown = new CountdownActor(COUNTDOWN_DURATION, FontManager.getPvzFont());
        countdown.setPosition(400f, 500f);
        hudStage.addActor(countdown);

        // Plant cards
        createPlantCards();

        // Sun HUD
        hudFont = FontManager.getPvzFont();
>>>>>>> origin
    }

    // ================== UI setup ==================

    private void layoutSeedBank() {
        float hudH = hudStage.getViewport().getWorldHeight();

        float originalW = seedBank.getWidth();
        float originalH = seedBank.getHeight();

        float scale = SEED_BANK_HEIGHT / originalH;
        float trayW = originalW * scale;
        float trayH = originalH * scale;

        seedBank.setSize(trayW, trayH);

        seedBank.setPosition(
                SEED_BANK_MARGIN_LEFT,
                hudH - trayH - SEED_BANK_MARGIN_TOP);
    }

    private void createPlantCards() {
        for (ItemType type : ItemType.values()) {
            PlantCard card = new PlantCard(type);
            plantCards.add(card);
            seedBank.addCard(card);
        }
    }

    public void onPlantCardClicked(PlantCard card) {
        if (!card.canUse(sunPoints))
            return;
        if (!spendSun(card.type.cost))
            return;

        card.triggerUse();
        // TODO: mode đặt plant
    }

    // ================== Lane & spawn helpers ==================

    private void initLanes() {
        for (int i = 0; i < ZOMBIE_LANE_COUNT; i++) {
            laneYs[i] = ZOMBIE_FIRST_LANE_Y + i * ZOMBIE_LANE_GAP_Y;
        }
    }

    private void startZombieWave() {
        spawnTimer = 0f;
        zombiesSpawnedInWave = 0;

        // 2 con demo ban đầu cho người chơi thấy
        spawnZombieInLane(0);
        spawnZombieInLane(ZOMBIE_LANE_COUNT - 1);
        zombiesSpawnedInWave = 2;

        scheduleNextSpawn();
    }

    private void scheduleNextSpawn() {
        nextSpawnTime = spawnTimer + MathUtils.random(MIN_SPAWN_INTERVAL, MAX_SPAWN_INTERVAL);
    }

    /** Spawn zombie ở lane cụ thể, đặt Y theo chiều cao zombie (đã scale). */
    private void spawnZombieInLane(int laneIndex) {
        float startX = WORLD_WIDTH + ZOMBIE_START_OFFSET_X + MathUtils.random(0f, 80f);
        laneIndex = MathUtils.clamp(laneIndex, 0, ZOMBIE_LANE_COUNT - 1);

        NormalZombie z = new NormalZombie();

        // Y giữa lane
        float laneCenterY = laneYs[laneIndex];

        // Đặt sao cho chân zombie nằm trên mặt đất:
        // laneCenterY ~ giữa ô cỏ, nên trừ đi nửa chiều cao zombie
        float zombieY = laneCenterY - z.getHeight() / 2f;

        z.setPosition(startX, zombieY);
        zombies.add(z);
    }

    private void spawnZombieInRandomLane() {
        int laneIndex = MathUtils.random(0, ZOMBIE_LANE_COUNT - 1);
        spawnZombieInLane(laneIndex);
    }

    private void updateZombieSpawning(float delta) {
        if (zombiesSpawnedInWave >= maxZombiesInWave)
            return;

        spawnTimer += delta;

        if (spawnTimer >= nextSpawnTime) {
            spawnZombieInRandomLane();
            zombiesSpawnedInWave++;
            scheduleNextSpawn();
        }
    }

    // ================== Lawn mowers ==================

    private void createLawnMowers() {
        float mowerX = 180f;

        for (int i = 0; i < ZOMBIE_LANE_COUNT; i++) {
            // vẫn dùng offset cũ, nếu lệch thì chỉnh thêm sau
            float laneY = ZOMBIE_FIRST_LANE_Y + i * ZOMBIE_LANE_GAP_Y - 50f;
            lawnMowers.add(new LawnMower(mowerX, laneY, WORLD_WIDTH));
        }
    }

    private void updateLawnMowers(float delta) {
        for (int i = lawnMowers.size - 1; i >= 0; i--) {
            LawnMower mower = lawnMowers.get(i);
            mower.update(delta, zombies);

            if (mower.isUsed()) {
                lawnMowers.removeIndex(i);
            }
        }
    }

    // ================== Sun helpers ==================

    public void addSun(int amount) {
        sunPoints += amount;
    }

    public boolean spendSun(int cost) {
        if (sunPoints < cost)
            return false;
        sunPoints -= cost;
        return true;
    }

    private void drawSunHud(SpriteBatch batch) {
        float sbX = seedBank.getX();
        float sbY = seedBank.getY();

        float textX = sbX + 55f;
        float textY = sbY + 42f;

        hudFont.draw(batch, String.valueOf(sunPoints), textX, textY);
    }

    // ================== Game logic ==================

    private void unlockPlantCards() {
        for (PlantCard card : plantCards) {
            card.setLockedByGame(false);
        }
    }

    private void updateState(float delta) {
        if (state != State.COUNTDOWN || countdown == null)
            return;

        if (countdown.isFinished()) {
            state = State.PLAYING;

            countdown.remove();
            countdown = null;

            unlockPlantCards();
            seedBank.setVisible(true);

            startZombieWave();
            createLawnMowers();
        }
    }

    private void updateGame(float delta) {
        if (state != State.PLAYING)
            return;

        updateZombieSpawning(delta);

        // update zombie + xoá nếu đi khỏi màn hình
        for (int i = zombies.size - 1; i >= 0; i--) {
            NormalZombie z = zombies.get(i);
            z.act(delta);

            if (z.getX() < -150f) {
                zombies.removeIndex(i);
                // TODO: xử lý khi zombie lọt qua nhà
            }
        }

        updateLawnMowers(delta);
        // TODO: check va chạm plant, bullet...
    }

    // ================== Render helpers ==================

    private void renderWorld(SpriteBatch batch) {
        float w = viewport.getWorldWidth();
        float h = viewport.getWorldHeight();

        if (state == State.COUNTDOWN) {
            backgroundManager.renderCount(batch, w, h);
            return;
        }

        backgroundManager.renderMain(batch, w, h);

        for (LawnMower mower : lawnMowers) {
            mower.render(batch);
        }

        for (NormalZombie z : zombies) {
            z.draw(batch, 1f);
        }

        if (state == State.PLAYING) {
            drawSunHud(batch);
        }
    }

    private void handleEscape() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new ResumeScreen(game, this));
        }
    }

    // ================== Screen lifecycle ==================

    @Override
    public void show() {
        // Click để tạo cây
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                Vector3 world = camera.unproject(new Vector3(screenX, screenY, 0));

                if (button == Input.Buttons.LEFT)
                    spawnPlant(PlantFactory.createPeashooter(world.x, world.y));
                else if (button == Input.Buttons.RIGHT)
                    spawnPlant(PlantFactory.createSunflower(world.x, world.y));

                return true;
            }
        });
    }

    @Override
    public void render(float delta) {
<<<<<<< HEAD

=======
>>>>>>> origin
        updateState(delta);
        updateGame(delta);
        handleEscape();

<<<<<<< HEAD
        // Clear screen
=======
>>>>>>> origin
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();
<<<<<<< HEAD
        float w = viewport.getWorldWidth();
        float h = viewport.getWorldHeight();

        if (state == State.COUNTDOWN) {
            backgroundManager.renderCount(batch, w, h);
        } else {
            backgroundManager.renderMain(batch, w, h);
        }
        batch.end();

        // Only update ECS when playing
        if (state == State.PLAYING) {
            sunSystem.update(plants, delta);
            attackSystem.update(plants, delta);

            // Render entities
            batch.setProjectionMatrix(camera.combined);
            renderSystem.update(entities);
        }

        // HUD
        hudStage.act(delta);
        hudStage.draw();

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            // TODO: mở Resume Screen
        }
    }

    private void updateState(float delta) {
        if (state == State.COUNTDOWN) {
            countdownTime -= delta;
            if (countdownTime <= 0f) {
                state = State.PLAYING;
                if (countdown != null) {
                    countdown.remove();
                    countdown = null;
                }
            }
        }
=======
        renderWorld(batch);
        batch.end();

        hudStage.act(delta);
        hudStage.draw();
>>>>>>> origin
    }

    @Override
    public void spawnSun(float x, float y, int amount) {
        Gdx.app.log("GameEvent", "Sun Spawn: " + x + "," + y);
    }

    @Override
    public void spawnProjectile(float x, float y, int damage, PlantDamageType type, Class<?> projectileClass) {
        if (projectileClass == PeaProjectile.class) {
            Entity pea = new PeaProjectile(x, y, damage);
            entities.add(pea);
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        hudStage.getViewport().update(width, height, true);
        layoutSeedBank();
    }

    @Override
    public void dispose() {
        batch.dispose();
        hudStage.dispose();
        backgroundManager.dispose();
<<<<<<< HEAD
        if (bgTex != null) bgTex.dispose();
=======
        seedBank.dispose();

        for (LawnMower mower : lawnMowers) {
            if (!mower.isUsed()) {
                mower.dispose();
            }
        }
>>>>>>> origin
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
}
