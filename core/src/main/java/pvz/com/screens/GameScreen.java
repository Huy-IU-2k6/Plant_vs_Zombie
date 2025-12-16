package pvz.com.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.List;

import pvz.com.entities.Entity;
import pvz.com.entities.plants.Plant;
import pvz.com.entities.plants.PlantType;
import pvz.com.entities.suns.Sun;
import pvz.com.entities.components.PlantDamageType;
import pvz.com.entities.projectiles.FrozenPeaProjectile;
import pvz.com.entities.projectiles.PeaProjectile;
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

// [2] IMPLEMENT INTERFACE
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

    // ===== Core refs =====
    private final Game game;
    private final SpriteBatch batch;
    private final Stage hudStage;
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
    private final HudController hudController;
    private final PlantGridController plantGridController;
    private final LawnMowerController lawnMowerController;
    private final ZombieWaveController zombieWaveController;
    private final WorldRenderer worldRenderer;

    // ===== World wrapper =====
    private final GameWorld gameWorld;

    // ===== Placement Controller =====
    private final PlantPlacementController plantPlacementController;

    public GameScreen(Game game) {
        this.game = game;

        this.batch = new SpriteBatch();

        // HUD stage
        this.hudStage = new Stage(new ScreenViewport());
        this.hudStage.getRoot().setUserObject(this);

        // World camera/viewport
        this.camera = new OrthographicCamera();
        this.viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        camera.position.set(WORLD_WIDTH / 2f, WORLD_HEIGHT / 2f, 0f);
        camera.update();

        GridConfig.init(viewport.getWorldWidth(), viewport.getWorldHeight());

        this.gameState = new GameState();

        // HUD
        this.hudController = new HudController(hudStage, COUNTDOWN_DURATION, INITIAL_SUN);

        // Grid
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

        // Placement
        this.plantPlacementController = new PlantPlacementController(
                viewport,
                hudController,
                plantGridController,
                gameWorld);

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

    // ================== Getters ==================

    public GameState getGameState() {
        return gameState;
    }

    public HudController getHudController() {
        return hudController;
    }

    // ================== Helpers ==================

    private boolean isPlaying() {
        return gameState.isPlaying();
    }

    private boolean isCountdown() {
        return gameState.isCountdown();
    }

    private boolean isGameOver() {
        return gameState.isGameOver();
    }

    // ================== HUD interaction ==================

    public void onPlantCardClicked(PlantCard card) {
        if (isGameOver()) return;
        // plantPlacementController.handleCardClicked(card, isPlaying());
    }

    public void onPlantCardDragged(PlantCard card, float screenX, float screenY) {
        if (isGameOver()) return;
        if (!isPlaying()) return;
        plantPlacementController.handleCardDragged(card, screenX, screenY, true);
    }

    // ================== Game State & Loop ==================

    private void updateState(float delta) {
        if (isGameOver()) return;
        if (!isCountdown()) return;

        if (hudController.isCountdownFinished()) {
            gameState.setState(GameState.State.PLAYING);
            hudController.onCountdownFinished();
            zombieWaveController.startWave();
            lawnMowerController.createLawnMowers();
            plantGridController.setEnabled(true);
        }
    }

    private void updateWorldControllers(float delta) {
        if (!isPlaying()) return;
        zombieWaveController.update(delta);
        lawnMowerController.update(delta, zombieWaveController.getZombies());
    }

    private void handleEscape() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new ResumeScreen(game, this));
        }
    }

    // ================== Screen ==================

    @Override
    public void show() {
        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(hudStage);
        
        // Sử dụng sunPickupSystem đã khởi tạo
        multiplexer.addProcessor(sunPickupSystem);

        Gdx.input.setInputProcessor(multiplexer);
        armingSystem = new ArmingSystem();
    }

    @Override
    public void render(float delta) {
        updateState(delta);
        updateWorldControllers(delta);
        handleEscape();

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        worldRenderer.render(batch, isCountdown(), isPlaying());
        batch.end();

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
            
            // Render entities
            batch.setProjectionMatrix(camera.combined);
            renderSystem.update(entities);
        }

        hudStage.act(delta);
        hudStage.draw();
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

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        hudController.resize(width, height);
    }

    @Override public void pause() { }
    @Override public void resume() { }
    @Override public void hide() { }

    @Override
    public void dispose() {
        batch.dispose();
        hudStage.dispose();
        hudController.dispose();
        worldRenderer.dispose();
        shapeRenderer.dispose();
        pvz.com.entities.Zombies.ZombieSounds.disposeAll();
    }

    // ================== IGameSpawner Implementation ==================
    @Override
    public void spawnSun(float x, float y, int amount) {
        Sun sun = new Sun(x, y, amount);
        entities.add(sun);
    }

    @Override
    public void spawnProjectile(float x, float y, int damage, PlantDamageType type, Class<?> projectileClass) {
        if (projectileClass == PeaProjectile.class) {
            Entity pea = new PeaProjectile(x, y, damage);
            entities.add(pea);
        } else if (projectileClass == FrozenPeaProjectile.class) {
            Entity frozenPea = new FrozenPeaProjectile(x, y, damage);
            entities.add(frozenPea);
        }
    }
    
    // ================== ISunReceiver Implementation ==================
    @Override
    public void addSun(int amount) {
        hudController.addSun(amount);
    }
}