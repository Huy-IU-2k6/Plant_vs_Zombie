package pvz.com.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.ArrayList;
import java.util.List;

import pvz.com.entities.Entity;
import pvz.com.entities.plants.Plant;
import pvz.com.items.PlantCard;

// [1] IMPORT CÁC SYSTEM VÀ INTERFACE
import pvz.com.systems.IGameSpawner;
import pvz.com.systems.ISunReceiver;
import pvz.com.systems.RenderSystem;
import pvz.com.systems.SunProductionSystem;
import pvz.com.systems.WallnutStateSystem;
import pvz.com.systems.PlantAttackSystem;
import pvz.com.systems.MovementSystem;
import pvz.com.systems.CollisionSystem;
import pvz.com.systems.SunPickupSystem;
import pvz.com.systems.AnimationSystem;
import pvz.com.systems.ArmingSystem;
import pvz.com.systems.ExplosionSystem;

import pvz.com.logic.GameState;
import pvz.com.logic.GameWorld;
import pvz.com.logic.HudController;
import pvz.com.logic.LawnMowerController;
import pvz.com.logic.PlantGridController;
import pvz.com.logic.PlantPlacementController;
import pvz.com.logic.WorldRenderer;
import pvz.com.logic.ZombieWaveController;
import pvz.com.managers.BackgroundManager;
import pvz.com.managers.DesignConfig;
import pvz.com.managers.GridConfig;

public class GameScreen implements Screen {

    // ===== World & layout =====
    private static final float WORLD_WIDTH = DesignConfig.BASE_SCREEN_W;
    private static final float WORLD_HEIGHT = DesignConfig.BASE_SCREEN_H;

    // ===== Game config =====
    private static final float COUNTDOWN_DURATION = 6f;
    private static final int INITIAL_SUN = 150;
    private static final float LEVEL_DURATION = 240f;

    // ===== Zombie lane config =====
    private static final float ZOMBIE_START_OFFSET_X = 200f;

    // ===== AUDIO: crossfade menu -> game =====
    private static final float CROSSFADE_DURATION = 0.8f;
    private static final float GAME_BGM_VOLUME = 1f;
    private static final String GAME_BGM_PATH = "musics/Grasswalk.mp3";

    private final Game game;
    private final SpriteBatch batch;

    // ===== Camera/viewport =====
    private final OrthographicCamera camera;
    private final Viewport viewport;
    private ShapeRenderer shapeRenderer;

    // ===== ECS Systems (KHAI BÁO LẠI CÁC BIẾN BỊ THIẾU) =====
    private final RenderSystem renderSystem;
    private final AnimationSystem animationSystem;
    private final SunProductionSystem sunSystem;
    private final PlantAttackSystem attackSystem;
    private final MovementSystem movementSystem;
    private final CollisionSystem collisionSystem;
    private final SunPickupSystem sunPickupSystem;
    private final WallnutStateSystem wallnutStateSystem;
    private final ExplosionSystem explosionSystem;
    private ArmingSystem armingSystem;

    // ===== ECS Data =====
    private final List<Entity> entities = new ArrayList<>();
    private final List<Plant> plants = new ArrayList<>();

    // ===== Game State =====
    private final GameState gameState;

    // ===== Controllers =====
    private final PlantGridController plantGridController;
    private final LawnMowerController lawnMowerController;
    private final ZombieWaveController zombieWaveController;
    private final WorldRenderer worldRenderer;

    // ===== World wrapper (chạy ECS) =====
    private final GameWorld gameWorld;

    // ===== Placement =====
    private final PlantPlacementController plantPlacementController;

    // ===== GameOver switch guard =====
    private boolean pushedGameOverScreen = false;

    // ===== Music =====
    private Music inheritedMenuMusic; // nhạc menu nhận từ MainMenu (có thể null)
    private float inheritedMenuStartVolume = 1f;

    private Music gameMusic;
    private float crossfadeTimer = 0f;
    private boolean startedFade = false;

    public GameScreen(Game game) {
        this(game, null);
    }

    public GameScreen(Game game, Music inheritedMenuMusic) {
        this.game = game;
        this.inheritedMenuMusic = inheritedMenuMusic;

        this.batch = new SpriteBatch();

        // HUD stage
        this.hudStage = new Stage(new ScreenViewport());
        this.hudStage.getRoot().setUserObject(this);

        // Camera/viewport
        this.camera = new OrthographicCamera();
        this.viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        camera.position.set(WORLD_WIDTH / 2f, WORLD_HEIGHT / 2f, 0f);
        camera.update();

        // GridConfig init theo world size
        GridConfig.init(viewport.getWorldWidth(), viewport.getWorldHeight());

        // State + HUD
        this.gameState = new GameState();
        this.hudController = new HudController(hudStage, COUNTDOWN_DURATION, INITIAL_SUN);

        // Grid controller
        this.plantGridController = new PlantGridController(entities, plants, camera);
        this.plantGridController.setEnabled(false);

        // Lawn mower
        this.lawnMowerController = new LawnMowerController(
                WORLD_WIDTH - 50f,
                DesignConfig.START_X - 150f);

        // Zombie waves
        this.zombieWaveController = new ZombieWaveController(
                WORLD_WIDTH,
                WORLD_HEIGHT,
                ZOMBIE_START_OFFSET_X,
                90,
                LEVEL_DURATION);

        // World renderer
        BackgroundManager backgroundManager = new BackgroundManager();
        this.worldRenderer = new WorldRenderer(
                backgroundManager,
                viewport,
                lawnMowerController,
                zombieWaveController);

        // GameWorld: nơi chạy ECS (attack/movement/collision/sun pickup/cleanup...)
        this.gameWorld = new GameWorld(
                gameState,
                hudController,
                entities,
                plants,
                camera,
                zombieWaveController,
                plantGridController,
                batch);

        // Placement controller
        this.plantPlacementController = new PlantPlacementController(
                viewport,
                hudController,
                plantGridController,
                gameWorld);

        // Debug renderer
        this.shapeRenderer = new ShapeRenderer();

        // [3] KHỞI TẠO CÁC SYSTEM (QUAN TRỌNG)
        // Nếu không khởi tạo ở đây, khi chạy render sẽ bị NullPointerException
        renderSystem = new RenderSystem(batch);
        animationSystem = new AnimationSystem();
        sunSystem = new SunProductionSystem(this, entities);
        attackSystem = new PlantAttackSystem(this, zombieWaveController);
        movementSystem = new MovementSystem();
        collisionSystem = new CollisionSystem(entities, zombieWaveController, plantGridController);
        sunPickupSystem = new SunPickupSystem(entities, camera, this);
        wallnutStateSystem = new WallnutStateSystem();
        explosionSystem = new ExplosionSystem(zombieWaveController, plantGridController);
        armingSystem = new ArmingSystem(); 
    }

    // ================== Public getters ==================

    public GameState getGameState() {
        return gameState;
    }

    public HudController getHudController() {
        return hudController;
    }

    // ================== Plant card interaction ==================

    public void onPlantCardClicked(PlantCard card) {
        if (card == null)
            return;
        if (gameState.isGameOver())
            return;

        // click-to-place (hiện tại xử lý trừ sun + cooldown ở controller)
        plantPlacementController.handleCardClicked(card, gameState.isPlaying());
    }

    public void onPlantCardDragged(PlantCard card, float screenX, float screenY) {
        if (card == null)
            return;
        if (gameState.isGameOver())
            return;

        plantPlacementController.handleCardDragged(card, screenX, screenY, gameState.isPlaying());
    }

    // ================== AUDIO ==================

    private void startCrossfadeIfNeeded() {
        if (startedFade)
            return;
        startedFade = true;

        // đảm bảo game music chạy
        if (gameMusic != null && !gameMusic.isPlaying()) {
            gameMusic.play();
        }

        // nếu nhạc menu có thì đảm bảo nó đang play
        if (inheritedMenuMusic != null) {
            inheritedMenuStartVolume = inheritedMenuMusic.getVolume();
            if (inheritedMenuStartVolume <= 0f)
                inheritedMenuStartVolume = 1f;

            if (!inheritedMenuMusic.isPlaying()) {
                inheritedMenuMusic.setLooping(true);
                inheritedMenuMusic.play();
            }
        }

        crossfadeTimer = CROSSFADE_DURATION;
    }

    private void updateCrossfade(float delta) {
        if (!startedFade)
            return;
        if (crossfadeTimer <= 0f)
            return;

        crossfadeTimer -= delta;
        float progress = 1f - Math.max(0f, crossfadeTimer / CROSSFADE_DURATION);

        if (gameMusic != null) {
            gameMusic.setVolume(GAME_BGM_VOLUME * progress);
        }

        if (inheritedMenuMusic != null) {
            inheritedMenuMusic.setVolume(inheritedMenuStartVolume * (1f - progress));
            if (crossfadeTimer <= 0f) {
                inheritedMenuMusic.stop();
                inheritedMenuMusic.dispose();
                inheritedMenuMusic = null;
            }
        }
    }

    private void pauseMusics() {
        if (inheritedMenuMusic != null)
            inheritedMenuMusic.pause();
        if (gameMusic != null)
            gameMusic.pause();
    }

    private void stopAndDisposeMusics() {
        if (inheritedMenuMusic != null) {
            inheritedMenuMusic.stop();
            inheritedMenuMusic.dispose();
            inheritedMenuMusic = null;
        }
        if (gameMusic != null) {
            gameMusic.stop();
            gameMusic.dispose();
            gameMusic = null;
        }
    }

    // ================== Game flow ==================

    private void updateState(float delta) {
        if (gameState.isGameOver())
            return;
        if (!gameState.isCountdown())
            return;

        if (hudController.isCountdownFinished()) {
            gameState.setState(GameState.State.PLAYING);
            hudController.onCountdownFinished();

            zombieWaveController.startWave();
            lawnMowerController.createLawnMowers();
            plantGridController.setEnabled(true);
        }
    }

    private void updateWorldControllers(float delta) {
        if (!gameState.isPlaying())
            return;

        zombieWaveController.update(delta);
        lawnMowerController.update(delta, zombieWaveController.getZombies());
    }

    private void handleEscape() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            // pause trước khi qua ResumeScreen (đỡ phụ thuộc hide())
            pauseMusics();
            game.setScreen(new ResumeScreen(game, this));
        }
    }

    private void pushGameOverOverlayIfNeeded() {
        if (!gameState.isGameOver())
            return;
        if (pushedGameOverScreen)
            return;

        pushedGameOverScreen = true;
        game.setScreen(new GameOverScreen(game, this, gameState.isPlayerWon()));
    }

    // ================== Render helpers ==================

    private void clearScreen() {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    private void renderWorldOnly(boolean isCountdown, boolean isPlaying) {
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        worldRenderer.render(batch, isCountdown, isPlaying);
        batch.end();
    }

    /**
     * Dùng cho GameOverScreen chụp lại frame cuối:
     * - KHÔNG update logic
     * - CHỈ render world + entities + HUD đúng trạng thái hiện tại
     */
    public void renderFrozen() {
        clearScreen();
        renderWorldOnly(gameState.isCountdown(), true);

        // entities (RenderSystem trong GameWorld sẽ begin/end batch riêng)
        batch.setProjectionMatrix(camera.combined);
        gameWorld.render(batch);

        hudStage.act(0f);
        hudStage.draw();
    }

    // ================== Screen ==================

    @Override
    public void show() {
        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(hudStage);
        multiplexer.addProcessor(gameWorld.getSunPickupSystem()); // input pickup sun
        Gdx.input.setInputProcessor(multiplexer);
        armingSystem = new ArmingSystem();
    }

    @Override
    public void render(float delta) {
        updateCrossfade(delta);

        handleEscape();

        // update countdown -> playing
        updateState(delta);

        // update controller “ngoài ECS” (zombie wave + lawnmower)
        updateWorldControllers(delta);

        clearScreen();

        // world layer
        renderWorldOnly(gameState.isCountdown(), gameState.isPlaying());

        // debug (nếu muốn)
        // drawDebugGrid();

        drawDebugGrid();

        // 3. Update & Vẽ ECS
        if (isPlaying()) {
            // Cập nhật tất cả hệ thống ECS
            sunSystem.update(delta);
            wallnutStateSystem.update(entities);
            explosionSystem.update(entities, delta); // Bom nổ
            armingSystem.update(entities, delta);
            animationSystem.update(entities, delta);
            attackSystem.update(plants, delta);      // Cây bắn
            movementSystem.update(entities, delta);
            collisionSystem.update(delta);           // Va chạm
            sunPickupSystem.update(delta);

            // GameWorld update (để đồng bộ nếu cần)
            gameWorld.update(delta);

            batch.setProjectionMatrix(camera.combined);
            gameWorld.render(batch);
        }

        // HUD luôn vẽ (countdown / playing / gameover đều có thể cần)
        hudStage.act(delta);
        hudStage.draw();

        // nếu game over -> đẩy overlay (1 lần)
        pushGameOverOverlayIfNeeded();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        hudController.resize(width, height);
    }

    @Override
    public void pause() {
        pauseMusics();
    }

    @Override
    public void resume() {
        // show() sẽ xử lý play lại
    }

    @Override
    public void hide() {
        // giữ giống bản cũ: chuyển screen thì tạm pause
        // (nếu muốn GameOver overlay vẫn nghe nhạc, có thể bỏ dòng này)
        pauseMusics();
    }

    @Override
    public void dispose() {
        stopAndDisposeMusics();

        batch.dispose();
        hudStage.dispose();
        hudController.dispose();
        worldRenderer.dispose();
        shapeRenderer.dispose();

        // nếu có ZombieSounds static
        pvz.com.entities.Zombies.ZombieSounds.disposeAll();
    }
}
