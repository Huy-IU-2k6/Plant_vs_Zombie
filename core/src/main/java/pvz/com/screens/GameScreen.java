package pvz.com.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
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

import pvz.com.Zombies.NormalZombie;
import pvz.com.managers.FontManager;
import pvz.com.managers.BackgroundManager;
import pvz.com.ui.CountdownActor;
import pvz.com.items.ItemType;
import pvz.com.items.PlantCard;
import pvz.com.items.SeedBank;
import pvz.com.items.LawnMower;

public class GameScreen implements Screen {

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

    private enum State {
        COUNTDOWN,
        PLAYING
    }

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

    public GameScreen(Game game) {
        this.game = game;

        batch = new SpriteBatch();

        // HUD stage (UI)
        hudStage = new Stage(new ScreenViewport());
        hudStage.getRoot().setUserObject(this);

        // Camera + viewport world 800x600
        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        camera.position.set(WORLD_WIDTH / 2f, WORLD_HEIGHT / 2f, 0f);
        camera.update();

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
        Gdx.input.setInputProcessor(hudStage);
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
            if (!mower.isUsed()) {
                mower.dispose();
            }
        }
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }
}
