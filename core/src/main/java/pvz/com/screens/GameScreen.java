package pvz.com.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.ArrayList;
import java.util.List;

import pvz.com.items.PlantCard;
import pvz.com.managers.BackgroundManager;
import pvz.com.managers.GridConfig;

// ===== ECS imports =====
import pvz.com.entities.Entity;
import pvz.com.entities.plants.Plant;
import pvz.com.entities.components.PlantDamageType;
import pvz.com.entities.projectiles.PeaProjectile;
import pvz.com.systems.IGameSpawner;
import pvz.com.systems.RenderSystem;
import pvz.com.systems.SunProductionSystem;
import pvz.com.systems.PlantAttackSystem;
import pvz.com.systems.MovementSystem;
import pvz.com.systems.ProjectileCollisionSystem;

// ===== Logic controllers =====
import pvz.com.logic.HudController;
import pvz.com.logic.LawnMowerController;
import pvz.com.logic.PlantGridController;
import pvz.com.logic.WorldRenderer;
import pvz.com.logic.ZombieWaveController;

public class GameScreen implements Screen, IGameSpawner {

    // ===== World & layout =====
    private static final float WORLD_WIDTH = 800f;
    private static final float WORLD_HEIGHT = 600f;

    // ===== Game config =====
    private static final float COUNTDOWN_DURATION = 6f;
    private static final int INITIAL_SUN = 150;

    // ===== Zombie lane config (dựa trên GridConfig) =====
    private static final int ZOMBIE_LANE_COUNT = GridConfig.ROWS;
    private static final float ZOMBIE_START_OFFSET_X = 50f;

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

    // ===== ECS: plants, projectiles, systems =====
    private final List<Entity> entities = new ArrayList<>();
    private final List<Plant> plants = new ArrayList<>();
    private final RenderSystem renderSystem;
    private final SunProductionSystem sunSystem;

    private final PlantAttackSystem attackSystem;
    private final MovementSystem movementSystem;
    private final ProjectileCollisionSystem projectileCollisionSystem;

    // ===== Controllers =====
    private final HudController hudController;
    private final PlantGridController plantGridController;
    private final LawnMowerController lawnMowerController;
    private final ZombieWaveController zombieWaveController;
    private final WorldRenderer worldRenderer;
    
    private State state = State.COUNTDOWN;
    

    public GameScreen(Game game) {
        this.game = game;

        // --- core ---
        batch = new SpriteBatch();

        // HUD stage (UI)
        hudStage = new Stage(new ScreenViewport());
        // để PlantCard lấy được GameScreen từ Stage
        hudStage.getRoot().setUserObject(this);

        // Camera + viewport world 800x600
        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        camera.position.set(WORLD_WIDTH / 2f, WORLD_HEIGHT / 2f, 0f);
        camera.update();

        // ===== Controllers =====
        hudController = new HudController(hudStage, COUNTDOWN_DURATION, INITIAL_SUN);

        plantGridController = new PlantGridController(entities, plants, camera);
        plantGridController.setEnabled(false);

        lawnMowerController = new LawnMowerController(ZOMBIE_LANE_COUNT, WORLD_WIDTH, 180f);
        zombieWaveController = new ZombieWaveController(
                WORLD_WIDTH,
                ZOMBIE_LANE_COUNT,
                ZOMBIE_START_OFFSET_X,
                20);

        BackgroundManager backgroundManager = new BackgroundManager();
        worldRenderer = new WorldRenderer(
                backgroundManager,
                viewport,
                lawnMowerController,
                zombieWaveController,
                hudController);

        // ===== ECS init =====
        renderSystem = new RenderSystem(batch);
        sunSystem = new SunProductionSystem(this, entities);
        attackSystem = new PlantAttackSystem(this);
        movementSystem = new MovementSystem();
        this.projectileCollisionSystem = new ProjectileCollisionSystem(entities, zombieWaveController);
    }

    // ================== HUD interaction ==================

    /** Được PlantCard gọi khi người chơi click 1 card. */
    public void onPlantCardClicked(PlantCard card) {
        if (!hudController.spendSun(card.type.cost))
            return;

        card.triggerUse();
        // TODO: sau này dùng PlantGridController để chọn loại plant, không dùng chuột
        // trái/phải nữa
    }

    // ================== Game state ==================

    private void updateState(float delta) {
        if (state != State.COUNTDOWN)
            return;

        if (hudController.isCountdownFinished()) {
            state = State.PLAYING;

            hudController.onCountdownFinished();

            zombieWaveController.startWave();
            lawnMowerController.createLawnMowers();
            plantGridController.setEnabled(true);
        }
    }

    private void updateGame(float delta) {
        if (state != State.PLAYING)
            return;

        zombieWaveController.update(delta);
        lawnMowerController.update(delta, zombieWaveController.getZombies());
        // TODO: check va chạm plant, bullet...
    }

    private void handleEscape() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new ResumeScreen(game, this));
        }
    }

    // ================== Screen lifecycle ==================

    @Override
    public void show() {
        // Dùng InputMultiplexer để:
        // - HUD (SeedBank, Countdown, button...) vẫn nhận input
        // - Đồng thời click xuống world để đặt cây theo grid
        InputMultiplexer multiplexer = new InputMultiplexer();

        // Ưu tiên HUD trước để click vào card không bị lọt xuống world
        multiplexer.addProcessor(hudStage);

        // Controller xử lý click xuống lawn
        multiplexer.addProcessor(plantGridController);

        Gdx.input.setInputProcessor(multiplexer);
    }

    @Override
    public void render(float delta) {
        // --- logic ---
        updateState(delta);
        updateGame(delta);
        handleEscape();

        // --- clear ---
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        batch.setProjectionMatrix(camera.combined);

        // --- world (background, zombie, mower, sun HUD) ---
        batch.begin();
        boolean isCountdown = (state == State.COUNTDOWN);
        boolean isPlaying = (state == State.PLAYING);
        worldRenderer.render(batch, isCountdown, isPlaying);
        batch.end();

        // --- ECS (plants, projectiles, sun system, attack system) ---
        if (state == State.PLAYING) {
            // update hệ thống logic
            sunSystem.update(delta); // sunflower sinh sun
            attackSystem.update(plants, delta); // plant bắn đạn (spawn PeaProjectile)

            // cho entity có MovementComponent di chuyển (đạn, zombie ECS nếu có)
            movementSystem.update(entities, delta); // đạn bay sang phải

            // xử lý đạn đâm vào zombie (trừ máu + xóa đạn / zombie chết)
            projectileCollisionSystem.update(delta);

            // render tất cả entity ECS (plant, đạn...)
            batch.setProjectionMatrix(camera.combined);
            renderSystem.update(entities);
        }

        // --- HUD (SeedBank, countdown, card...) ---
        hudStage.act(delta);
        hudStage.draw();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        hudController.resize(width, height);
    }

    @Override
    public void dispose() {
        batch.dispose();
        hudStage.dispose();
        hudController.dispose();
        worldRenderer.dispose();
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

    // ================== IGameSpawner (cho ECS gọi ngược lại) ==================

    @Override
    public void spawnSun(float x, float y, int amount) {
        // TODO: sau này tạo SunEntity và add vào entities
        Gdx.app.log("GameEvent", "Sun Spawn: " + x + "," + y + " amount=" + amount);
        addSun(amount);
    }

    @Override
    public void spawnProjectile(float x, float y, int damage,
            PlantDamageType type, Class<?> projectileClass) {
        if (projectileClass == PeaProjectile.class) {
            Entity pea = new PeaProjectile(x, y, damage);
            entities.add(pea);
        }
        // sau này nếu có nhiều loại đạn thì thêm else-if ở đây
    }

    // convenience cho chỗ khác gọi nếu cần
    public void addSun(int amount) {
        hudController.addSun(amount);
    }

    public boolean spendSun(int cost) {
        return hudController.spendSun(cost);
    }
}
