package pvz.com.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
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
import pvz.com.entities.suns.Sun;
import pvz.com.entities.projectiles.PeaProjectile;
import pvz.com.entities.projectiles.FrozenPeaProjectile;
import pvz.com.entities.components.PlantDamageType;
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

// [FIX QUAN TRỌNG] Thêm implements IGameSpawner, ISunReceiver để tránh lỗi ClassCastException
public class GameScreen implements Screen, IGameSpawner, ISunReceiver {

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

    private final Game game;
    private final SpriteBatch batch;

    // ===== Camera/viewport =====
    private final OrthographicCamera camera;
    private final Viewport viewport;
    private ShapeRenderer shapeRenderer;

    // ===== ECS Systems =====
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

    // ===== World wrapper =====
    private final GameWorld gameWorld;

    // ===== Placement =====
    private final PlantPlacementController plantPlacementController;

    // ===== GameOver switch guard =====
    private boolean pushedGameOverScreen = false;

    // ===== Music =====
    private Music inheritedMenuMusic; 
    private float inheritedMenuStartVolume = 1f;

    private Music gameMusic;
    private float crossfadeTimer = 0f;
    private boolean startedFade = false;
    
    private final Stage hudStage;
    private final HudController hudController;

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
                DesignConfig.START_X - 80f);

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

        // GameWorld
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

        // [3] KHỞI TẠO CÁC SYSTEM
        renderSystem = new RenderSystem(batch);
        animationSystem = new AnimationSystem();
        
        // Vì class đã implements IGameSpawner nên (IGameSpawner)this là hợp lệ
        sunSystem = new SunProductionSystem(this, entities);
        attackSystem = new PlantAttackSystem(this, zombieWaveController);
        
        movementSystem = new MovementSystem();
        collisionSystem = new CollisionSystem(entities, zombieWaveController, plantGridController);
        
        // Vì class đã implements ISunReceiver nên (ISunReceiver)this là hợp lệ
        sunPickupSystem = new SunPickupSystem(entities, camera, this);
        
        wallnutStateSystem = new WallnutStateSystem();
        explosionSystem = new ExplosionSystem(zombieWaveController, plantGridController);
        armingSystem = new ArmingSystem(); 
    }

    // ================== Helpers ==================

    private boolean isPlaying() {
        return gameState.isPlaying();
    }

    private void drawDebugGrid() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(new Color(1f, 1f, 1f, 0.3f));

        for (int row = 0; row < GridConfig.ROWS; row++) {
            for (int col = 0; col < GridConfig.COLS; col++) {
                float x = GridConfig.getCellOriginX(col);
                float y = GridConfig.getCellOriginY(row);
                shapeRenderer.rect(x, y, GridConfig.CELL_WIDTH, GridConfig.CELL_HEIGHT);
            }
        }
        shapeRenderer.end();
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
        if (card == null || gameState.isGameOver()) return;
        plantPlacementController.handleCardClicked(card, gameState.isPlaying());
    }

    public void onPlantCardDragged(PlantCard card, float screenX, float screenY) {
        if (card == null || gameState.isGameOver()) return;
        plantPlacementController.handleCardDragged(card, screenX, screenY, gameState.isPlaying());
    }

    // ================== AUDIO ==================
    private void startCrossfadeIfNeeded() {
        if (startedFade) return;
        startedFade = true;
        if (gameMusic != null && !gameMusic.isPlaying()) gameMusic.play();
        if (inheritedMenuMusic != null) {
            inheritedMenuStartVolume = inheritedMenuMusic.getVolume();
            if (inheritedMenuStartVolume <= 0f) inheritedMenuStartVolume = 1f;
            if (!inheritedMenuMusic.isPlaying()) {
                inheritedMenuMusic.setLooping(true);
                inheritedMenuMusic.play();
            }
        }
        crossfadeTimer = CROSSFADE_DURATION;
    }

    private void updateCrossfade(float delta) {
        if (!startedFade || crossfadeTimer <= 0f) return;
        crossfadeTimer -= delta;
        float progress = 1f - Math.max(0f, crossfadeTimer / CROSSFADE_DURATION);
        if (gameMusic != null) gameMusic.setVolume(GAME_BGM_VOLUME * progress);
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
        if (inheritedMenuMusic != null) inheritedMenuMusic.pause();
        if (gameMusic != null) gameMusic.pause();
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
        if (gameState.isGameOver()) return;
        if (!gameState.isCountdown()) return;

        if (hudController.isCountdownFinished()) {
            gameState.setState(GameState.State.PLAYING);
            hudController.onCountdownFinished();
            zombieWaveController.startWave();
            lawnMowerController.createLawnMowers();
            plantGridController.setEnabled(true);
        }
    }

    private void updateWorldControllers(float delta) {
        if (!gameState.isPlaying()) return;
        zombieWaveController.update(delta);
        lawnMowerController.update(delta, zombieWaveController.getZombies());
    }

    private void handleEscape() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            pauseMusics();
            game.setScreen(new ResumeScreen(game, this));
        }
    }

    private void pushGameOverOverlayIfNeeded() {
        if (!gameState.isGameOver() || pushedGameOverScreen) return;
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

    public void renderFrozen() {
        clearScreen();
        renderWorldOnly(gameState.isCountdown(), true);
        
        batch.setProjectionMatrix(camera.combined);
        renderSystem.update(entities); 

        hudStage.act(0f);
        hudStage.draw();
    }

    // ================== Screen ==================
    @Override
    public void show() {
        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(hudStage);
        multiplexer.addProcessor(gameWorld.getSunPickupSystem());
        Gdx.input.setInputProcessor(multiplexer);
        armingSystem = new ArmingSystem();
    }

    @Override
    public void render(float delta) {
        updateCrossfade(delta);
        handleEscape();
        updateState(delta);
        updateWorldControllers(delta);

        clearScreen();

        renderWorldOnly(gameState.isCountdown(), gameState.isPlaying());
        drawDebugGrid(); 

        if (isPlaying()) {
            // ECS Update
            sunSystem.update(delta);
            wallnutStateSystem.update(entities);
            explosionSystem.update(entities, delta); 
            armingSystem.update(entities, delta); 
            animationSystem.update(entities, delta); 
            attackSystem.update(plants, delta); 
            movementSystem.update(entities, delta);
            collisionSystem.update(delta);
            sunPickupSystem.update(delta);
            gameWorld.update(delta);

            // Vẽ Entities
            batch.setProjectionMatrix(camera.combined);
            renderSystem.update(entities); 
        }

        hudStage.act(delta);
        hudStage.draw();

        pushGameOverOverlayIfNeeded();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        hudController.resize(width, height);
    }

    @Override
    public void pause() { pauseMusics(); }
    @Override
    public void resume() { }
    @Override
    public void hide() { pauseMusics(); }
    
    @Override
    public void dispose() {
        stopAndDisposeMusics();
        batch.dispose();
        hudStage.dispose();
        hudController.dispose();
        worldRenderer.dispose();
        shapeRenderer.dispose();
        pvz.com.entities.Zombies.ZombieSounds.disposeAll();
    }

    // ================== IGameSpawner Implementation (FIX LỖI ÉP KIỂU) ==================
    @Override
    public void spawnSun(float x, float y, int amount) {
        entities.add(new Sun(x, y, amount));
    }

    @Override
    public void spawnProjectile(float x, float y, int damage, PlantDamageType type, Class<?> projectileClass) {
        if (projectileClass == PeaProjectile.class) {
            entities.add(new PeaProjectile(x, y, damage));
        } else if (projectileClass == FrozenPeaProjectile.class) {
            entities.add(new FrozenPeaProjectile(x, y, damage));
        }
    }

    // ================== ISunReceiver Implementation (FIX LỖI ÉP KIỂU) ==================
    @Override
    public void addSun(int amount) {
        hudController.addSun(amount);
    }
}