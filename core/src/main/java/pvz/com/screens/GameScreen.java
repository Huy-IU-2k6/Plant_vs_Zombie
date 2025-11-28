package pvz.com.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

import pvz.com.entities.Zombies.NormalZombie;
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
import pvz.com.entities.plants.PlantType;
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

    // ===== Game config =====
    private static final float COUNTDOWN_DURATION = 6f;
    private static final int INITIAL_SUN = 150;

    // ===== Zombie lane config =====
    private static final int ZOMBIE_LANE_COUNT = 5;
    private static final float ZOMBIE_START_OFFSET_X = 50f;
    private static final float ZOMBIE_FIRST_LANE_Y = 100f;
    private static final float ZOMBIE_LANE_GAP_Y = 100f;

    // ===== SeedBank layout =====
    private static final float SEED_BANK_HEIGHT = 110f;
    private static final float SEED_BANK_MARGIN_TOP = 20f;
    private static final float SEED_BANK_MARGIN_LEFT = 50f;

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

    private enum State { COUNTDOWN, PLAYING }
    private State state = State.COUNTDOWN;

    // ===== Sun HUD =====
    private int sunPoints = INITIAL_SUN;

    // ===== Entities =====
    private final Array<NormalZombie> zombies = new Array<>();
    private final Array<LawnMower> lawnMowers = new Array<>();
    private final List<Entity> entities = new ArrayList<>();
    private final List<Plant> plants = new ArrayList<>();

    private final RenderSystem renderSystem = new RenderSystem(new SpriteBatch());
    private final SunProductionSystem sunSystem = new SunProductionSystem(this);
    private final PlantAttackSystem attackSystem = new PlantAttackSystem(this);

    // ===== Zombie spawn control =====
    private float spawnTimer = 0f;
    private float nextSpawnTime = 0f;
    private int zombiesSpawnedInWave = 0;
    private int maxZombiesInWave = 20;

    private static final float MIN_SPAWN_INTERVAL = 2.2f;
    private static final float MAX_SPAWN_INTERVAL = 4.0f;

    private final float[] laneYs = new float[ZOMBIE_LANE_COUNT];
    // Cấu hình lưới (Grid Config) - Chỉnh số cho khớp với ảnh background của bạn
    private static final float GRID_OFFSET_X = 40f; // Mép trái sân cỏ bắt đầu từ x = 40
    private static final float GRID_OFFSET_Y = 100f; // Mép dưới sân cỏ bắt đầu từ y = 100
    private static final float CELL_WIDTH = 80f;     // Chiều rộng 1 ô
    private static final float CELL_HEIGHT = 100f;   // Chiều cao 1 ô (bằng khoảng cách giữa các làn)
    private static final int GRID_COLS = 9;
    private static final int GRID_ROWS = 5;
    public GameScreen(Game game) {
        this.game = game;
        this.batch = new SpriteBatch();

        // HUD stage
        hudStage = new Stage(new ScreenViewport());
        hudStage.getRoot().setUserObject(this);

        // Camera
        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        camera.position.set(WORLD_WIDTH / 2f, WORLD_HEIGHT / 2f, 0f);
        camera.update();

        // Background
        backgroundManager = new BackgroundManager();

        // Lanes
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

        // Font
        hudFont = FontManager.getPvzFont();
    }

    // ================== UI setup ==================

    private void layoutSeedBank() {
        float hudH = hudStage.getViewport().getWorldHeight();

        float scale = SEED_BANK_HEIGHT / seedBank.getHeight();
        float trayW = seedBank.getWidth() * scale;
        float trayH = seedBank.getHeight() * scale;

        seedBank.setSize(trayW, trayH);
        seedBank.setPosition(SEED_BANK_MARGIN_LEFT, hudH - trayH - SEED_BANK_MARGIN_TOP);
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
        // TODO: placing plant mode
    }

    // ================== Lane helpers ==================

    private void initLanes() {
        for (int i = 0; i < ZOMBIE_LANE_COUNT; i++) {
            laneYs[i] = ZOMBIE_FIRST_LANE_Y + i * ZOMBIE_LANE_GAP_Y;
        }
    }

    // ================== Zombie spawn helpers ==================

    private void startZombieWave() {
        spawnTimer = 0f;
        zombiesSpawnedInWave = 0;

        spawnZombieInLane(0);
        spawnZombieInLane(ZOMBIE_LANE_COUNT - 1);
        zombiesSpawnedInWave = 2;

        scheduleNextSpawn();
    }

    private void scheduleNextSpawn() {
        nextSpawnTime = spawnTimer + MathUtils.random(MIN_SPAWN_INTERVAL, MAX_SPAWN_INTERVAL);
    }

    private void spawnZombieInLane(int laneIndex) {
        float startX = WORLD_WIDTH + ZOMBIE_START_OFFSET_X + MathUtils.random(0f, 80f);
        laneIndex = MathUtils.clamp(laneIndex, 0, ZOMBIE_LANE_COUNT - 1);

        NormalZombie z = new NormalZombie();
        float laneCenterY = laneYs[laneIndex];
        float zombieY = laneCenterY - z.getHeight() / 2f;

        z.setPosition(startX, zombieY);
        zombies.add(z);
    }

    private void spawnZombieInRandomLane() {
        spawnZombieInLane(MathUtils.random(0, ZOMBIE_LANE_COUNT - 1));
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

    // ================== Lawn Mowers ==================

    private void createLawnMowers() {
        float mowerX = 180f;

        for (int i = 0; i < ZOMBIE_LANE_COUNT; i++) {
            float laneY = ZOMBIE_FIRST_LANE_Y + i * ZOMBIE_LANE_GAP_Y - 50f;
            lawnMowers.add(new LawnMower(mowerX, laneY, WORLD_WIDTH));
        }
    }

    private void updateLawnMowers(float delta) {
        for (int i = lawnMowers.size - 1; i >= 0; i--) {
            LawnMower mower = lawnMowers.get(i);
            mower.update(delta, zombies);

            if (mower.isUsed())
                lawnMowers.removeIndex(i);
        }
    }

    // ================== Sun helpers ==================

    public void addSun(int amount) {
        sunPoints += amount;
    }

    public boolean spendSun(int cost) {
        if (sunPoints < cost) return false;
        sunPoints -= cost;
        return true;
    }

    private void drawSunHud(SpriteBatch batch) {
        float textX = seedBank.getX() + 55f;
        float textY = seedBank.getY() + 42f;
        hudFont.draw(batch, String.valueOf(sunPoints), textX, textY);
    }

    // ================== State update ==================

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

        for (int i = zombies.size - 1; i >= 0; i--) {
            NormalZombie z = zombies.get(i);
            z.act(delta);

            if (z.getX() < -150f)
                zombies.removeIndex(i);
        }

        updateLawnMowers(delta);
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

        for (LawnMower mower : lawnMowers)
            mower.render(batch);

        for (NormalZombie z : zombies)
            z.draw(batch, 1f);

        if (state == State.PLAYING)
            drawSunHud(batch);
    }

    private void handleEscape() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new ResumeScreen(game, this));
        }
    }

    // ================== Screen lifecycle ==================

    // Đảm bảo bạn đã khai báo các hằng số này ở đầu class GameScreen
    // private static final float GRID_OFFSET_X = 40f; 
    // private static final float GRID_OFFSET_Y = 100f; 
    // private static final float CELL_WIDTH = 80f;
    // private static final float CELL_HEIGHT = 95f; // Hoặc 100f tùy khoảng cách làn của bạn
    // private static final int GRID_COLS = 9;
    // private static final int GRID_ROWS = 5;

    @Override
    public void show() {
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                // 1. Chuyển đổi tọa độ màn hình sang tọa độ thế giới game
                Vector3 world = camera.unproject(new Vector3(screenX, screenY, 0));

                // 2. Tính toán xem đang click vào Hàng (row) nào, Cột (col) nào
                int col = (int) ((world.x - GRID_OFFSET_X) / CELL_WIDTH);
                int row = (int) ((world.y - GRID_OFFSET_Y) / CELL_HEIGHT);

                // 3. Kiểm tra xem click có nằm trong sân cỏ không (Nếu ra ngoài thì không trồng)
                if (col < 0 || col >= GRID_COLS || row < 0 || row >= GRID_ROWS) {
                    return false;
                }

                // 4. Tính toán tọa độ vẽ cây (để cây nằm chính giữa ô đất)
                float plantX = GRID_OFFSET_X + col * CELL_WIDTH;
                float plantY = GRID_OFFSET_Y + row * CELL_HEIGHT;

                // 5. Gọi Factory để tạo cây (Dùng enum PlantType cho gọn)
                if (button == Input.Buttons.LEFT) {
                    // Chuột TRÁI -> Trồng Peashooter
                    spawnPlant(PlantFactory.createPlant(PlantType.PEASHOOTER, plantX, plantY, col, row));
                    
                } else if (button == Input.Buttons.RIGHT) {
                    // Chuột PHẢI -> Trồng Sunflower
                    spawnPlant(PlantFactory.createPlant(PlantType.SUNFLOWER, plantX, plantY, col, row));
                    
                } else if (button == Input.Buttons.MIDDLE) {
                    // Chuột GIỮA -> Trồng Cherry Bomb (Thử nghiệm nổ)
                    spawnPlant(PlantFactory.createPlant(PlantType.CHERRY_BOMB, plantX, plantY, col, row));
                }

                return true;
            }
        });
    }

    private void spawnPlant(Plant plant) {
        entities.add(plant);
        plants.add(plant);
    }

    @Override
    public void render(float delta) {
        updateState(delta);
        updateGame(delta);
        handleEscape();

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        renderWorld(batch);
        batch.end();

        hudStage.act(delta);
        hudStage.draw();
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
        seedBank.dispose();

        for (LawnMower mower : lawnMowers) {
            if (!mower.isUsed())
                mower.dispose();
        }
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
}
