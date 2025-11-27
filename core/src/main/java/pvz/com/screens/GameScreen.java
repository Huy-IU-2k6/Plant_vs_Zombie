package pvz.com.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.ArrayList;
import java.util.List;

import pvz.com.Zombies.NormalZombie;
import pvz.com.entities.Entity;
import pvz.com.entities.components.PlantDamageType;
import pvz.com.entities.plants.Plant;
import pvz.com.entities.projectiles.PeaProjectile;
import pvz.com.factories.PlantFactory;
import pvz.com.items.PlantCard;
import pvz.com.managers.BackgroundManager;
import pvz.com.managers.GridConfig;
import pvz.com.systems.IGameSpawner;
import pvz.com.systems.PlantAttackSystem;
import pvz.com.systems.RenderSystem;
import pvz.com.systems.SunProductionSystem;

// ==== Controllers ====
import pvz.com.logic.HudController;
import pvz.com.logic.LawnMowerController;
import pvz.com.logic.PlantGridController;
import pvz.com.logic.WorldRenderer;
import pvz.com.logic.ZombieWaveController;

public class GameScreen implements Screen, IGameSpawner {

    // ===== World & layout =====
    private static float WORLD_WIDTH;
    private static float WORLD_HEIGHT;

    // ===== Game config =====
    private static final float COUNTDOWN_DURATION = 6f;
    private static final int INITIAL_SUN = 150;

    // ===== Zombie lane config (dựa trên GridConfig) =====
    private static final int ZOMBIE_LANE_COUNT = GridConfig.ROWS;
    private static final float ZOMBIE_START_OFFSET_X = 50f;
    private static final int MAX_ZOMBIES_IN_WAVE = 15;

    public enum State {
        COUNTDOWN,
        PLAYING
    }

    // ===== Core refs =====
    private final Game game;
    private final SpriteBatch batch;
    private final OrthographicCamera camera;
    private final Viewport viewport;

    private final Stage hudStage;

    // Background + zombie list cho renderer / controller dùng chung
    private final BackgroundManager backgroundManager;
    private final Array<NormalZombie> zombies = new Array<>();

    // ===== Controllers =====
    private final HudController hudController;
    private final LawnMowerController lawnMowerController;
    private final PlantGridController plantGridController;
    private final ZombieWaveController zombieWaveController;
    private final WorldRenderer worldRenderer;

    // ===== Game state =====
    private State state = State.COUNTDOWN;

    // ===== ECS: plants, projectiles, systems =====
    private final List<Entity> entities = new ArrayList<>();
    private final List<Plant> plants = new ArrayList<>();
    private final RenderSystem renderSystem;
    private final SunProductionSystem sunSystem;
    private final PlantAttackSystem attackSystem;

    public GameScreen(Game game) {
        this.game = game;

        // --- core ---
        batch = new SpriteBatch();

        // Camera + viewport world 800x600
        WORLD_WIDTH = Gdx.graphics.getWidth();
        WORLD_HEIGHT = Gdx.graphics.getHeight();
        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        camera.position.set(WORLD_WIDTH / 2f, WORLD_HEIGHT / 2f, 0f);
        camera.update();

        backgroundManager = new BackgroundManager();

        // HUD stage dùng FitViewport cùng kích thước world để toạ độ khớp
        hudStage = new Stage(new FitViewport(WORLD_WIDTH, WORLD_HEIGHT));

        // ===== Controllers init =====
        hudController = new HudController(
                hudStage,
                COUNTDOWN_DURATION,
                INITIAL_SUN);

        // Lawn mower: (worldWidth, laneCount)
        lawnMowerController = new LawnMowerController(
                WORLD_WIDTH,
                ZOMBIE_LANE_COUNT);

        // Wave spawn: (worldWidth, laneCount, offsetX, zombies list, maxZombies)
        zombieWaveController = new ZombieWaveController(
                WORLD_WIDTH,
                ZOMBIE_LANE_COUNT,
                ZOMBIE_START_OFFSET_X,
                zombies,
                MAX_ZOMBIES_IN_WAVE);

        // Controller đặt plant trên grid, tự xử lý input + spawn plant
        plantGridController = new PlantGridController(
                entities,
                plants,
                camera);
        plantGridController.setEnabled(false); // chỉ bật sau khi hết countdown
        plantGridController.initDemoPlants();

        // Renderer: vẽ background + mower + zombies
        // SeedBank & font không cần cho WorldRenderer (ta để nó = null và không gọi
        // showSunHud)
        worldRenderer = new WorldRenderer(
                viewport,
                backgroundManager,
                lawnMowerController.getLawnMowers(),
                zombies,
                null,
                null);

        // ===== ECS init =====
        renderSystem = new RenderSystem(batch);
        sunSystem = new SunProductionSystem(this);
        attackSystem = new PlantAttackSystem(this);
    }

    // ================== UI / HUD callbacks ==================

    /** Bị PlantCard gọi khi người chơi click một card. */
    public void onPlantCardClicked(PlantCard card) {
        // Uỷ quyền cho HudController xử lý sun / cooldown
        hudController.onPlantCardClicked(card);
        // TODO: sau này nối với PlantGridController để biết loại plant đang chọn
    }

    // ================== Game state ==================

    private void updateState(float delta) {
        if (state != State.COUNTDOWN)
            return;

        // HudController tự cập nhật countdown trong Stage.act()
        if (hudController.isCountdownFinished()) {
            state = State.PLAYING;

            // Ẩn countdown, mở SeedBank, unlock card
            hudController.onCountdownFinished();

            // Cho phép đặt cây
            plantGridController.setEnabled(true);

            // Tạo mower và bắt đầu wave
            lawnMowerController.createLawnMowers();
            zombieWaveController.startWave();
        }
    }

    private void updateGame(float delta) {
        if (state != State.PLAYING)
            return;

        // Spawn + update zombie (chỉ spawn, zombie tự update bên trong)
        zombieWaveController.update(delta);

        // Lawn mower xử lý va chạm và remove khi đã dùng xong
        lawnMowerController.update(delta, zombies);

        // TODO: xử lý khi zombie lọt qua nhà (thua)
    }

    // ================== Input ==================

    @Override
    public void show() {
        InputMultiplexer multiplexer = new InputMultiplexer();

        // Ưu tiên HUD stage
        multiplexer.addProcessor(hudStage);

        // Controller xử lý click trên lawn để đặt plant
        multiplexer.addProcessor(plantGridController);

        Gdx.input.setInputProcessor(multiplexer);
    }

    private void handleEscape() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new ResumeScreen(game, this));
        }
    }

    // ================== IGameSpawner (cho ECS) ==================

    @Override
    public void spawnSun(float x, float y, int amount) {
        // Sau này nếu có SunEntity thì spawn, giờ cộng thẳng HUD
        hudController.addSun(amount);
    }

    @Override
    public void spawnProjectile(float x, float y, int damage,
            PlantDamageType type, Class<?> projectileClass) {
        if (projectileClass == PeaProjectile.class) {
            Entity pea = new PeaProjectile(x, y, damage);
            entities.add(pea);
        }
        // sau này thêm loại đạn khác thì else-if ở đây
    }

    // ================== Render ==================

    @Override
    public void render(float delta) {
        // --- logic ---
        updateState(delta);
        updateGame(delta);
        handleEscape();

        // ECS systems cho plant / sun / bullet
        if (state == State.PLAYING) {
            sunSystem.update(plants, delta);
            attackSystem.update(plants, delta);
        }

        // --- clear ---
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // --- world (background + zombie + mower + sun text) ---
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        // Không cho WorldRenderer vẽ sun HUD (showSunHud = false, sunPoints = 0)
        worldRenderer.render(
                batch,
                state == State.COUNTDOWN,
                false,
                0);

        // Vẽ sun HUD bằng HudController (dùng SeedBank trong HUD)
        if (state == State.PLAYING) {
            hudController.drawSunHud(batch);
        }
        batch.end();

        // --- ECS render (plants, projectiles, ...) ---
        batch.setProjectionMatrix(camera.combined);
        renderSystem.update(entities);

        // --- HUD (SeedBank, countdown, card, ...) ---
        hudController.actAndDraw(delta);
    }

    // ================== Screen lifecycle ==================

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        // HudController tự update viewport của Stage + layout SeedBank
        hudController.resize(width, height);
    }

    @Override
    public void dispose() {
        batch.dispose();
        hudController.dispose();
        hudStage.dispose();
        lawnMowerController.dispose();
        // entities / plants: nếu có texture/sound riêng thì tự dispose bên trong
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
