package pvz.com.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.graphics.Color;
import java.util.ArrayList;
import java.util.List;

import pvz.com.entities.Entity;
import pvz.com.entities.plants.Plant;
import pvz.com.entities.plants.PlantType;
import pvz.com.factories.PlantFactory;
import pvz.com.entities.suns.Sun;
import pvz.com.entities.components.PlantDamageType;
import pvz.com.entities.projectiles.FrozenPeaProjectile;
import pvz.com.entities.projectiles.PeaProjectile;
import pvz.com.items.ItemType;
import pvz.com.items.PlantCard;

import pvz.com.systems.IGameSpawner;
import pvz.com.systems.RenderSystem;
import pvz.com.systems.SunProductionSystem;
import pvz.com.systems.WallnutStateSystem;
import pvz.com.systems.PlantAttackSystem;
import pvz.com.systems.MovementSystem;
import pvz.com.systems.CollisionSystem;
import pvz.com.systems.SunPickupSystem;
import pvz.com.systems.AnimationSystem;
import pvz.com.systems.ISunReceiver;
import pvz.com.systems.ExplosionSystem;

import pvz.com.logic.GameState;
import pvz.com.logic.HudController;
import pvz.com.logic.LawnMowerController;
import pvz.com.logic.PlantGridController;
import pvz.com.logic.WorldRenderer;
import pvz.com.logic.ZombieWaveController;
import pvz.com.managers.BackgroundManager;
import pvz.com.managers.GridConfig;
import pvz.com.managers.DesignConfig;

public class GameScreen implements Screen, IGameSpawner, ISunReceiver {

    // ===== World & layout =====
    private static final float WORLD_WIDTH = DesignConfig.BASE_SCREEN_W;
    private static final float WORLD_HEIGHT = DesignConfig.BASE_SCREEN_H;

    // ===== Game config =====
    private static final float COUNTDOWN_DURATION = 6f;
    private static final int INITIAL_SUN = 150;
    private static final float LEVEL_DURATION = 240f; 

    // ===== Zombie lane config =====
    private static final int ZOMBIE_LANE_COUNT = GridConfig.ROWS;
    private static final float ZOMBIE_START_OFFSET_X = 200f;

    // ===== Core refs =====
    private final Game game;
    private final SpriteBatch batch;
    private final Stage hudStage;
    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final WallnutStateSystem wallnutStateSystem;
    private final ExplosionSystem explosionSystem;
    private ShapeRenderer shapeRenderer;

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

    // ===== ECS Systems (MỚI THÊM VÀO) =====
    private final RenderSystem renderSystem;
    private final AnimationSystem animationSystem;
    private final SunProductionSystem sunSystem;
    private final PlantAttackSystem attackSystem;
    private final MovementSystem movementSystem;
    private final CollisionSystem collisionSystem;
    private final SunPickupSystem sunPickupSystem;

    public GameScreen(Game game) {
        this.game = game;

        // --- core ---
        this.batch = new SpriteBatch();
        this.hudStage = new Stage(new ScreenViewport());
        this.hudStage.getRoot().setUserObject(this);

        this.camera = new OrthographicCamera();
        this.viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        camera.position.set(WORLD_WIDTH / 2f, WORLD_HEIGHT / 2f, 0f);
        camera.update();

        // Init GridConfig
        GridConfig.init(viewport.getWorldWidth(), viewport.getWorldHeight());

        // ===== GameState =====
        this.gameState = new GameState();

        // ===== Controllers =====
        this.hudController = new HudController(hudStage, COUNTDOWN_DURATION, INITIAL_SUN);
        this.plantGridController = new PlantGridController(entities, plants, camera);
        plantGridController.setEnabled(false);

        // [GIỮ NGUYÊN NHƯ BẢN GỐC CỦA BẠN]
        this.lawnMowerController = new LawnMowerController(
                WORLD_WIDTH - 50f,
                DesignConfig.START_X - 150f
        );

        this.zombieWaveController = new ZombieWaveController(
                WORLD_WIDTH,
                WORLD_HEIGHT, // Sửa lại constructor cho khớp bản gốc
                ZOMBIE_START_OFFSET_X,
                90, 
                LEVEL_DURATION);

        BackgroundManager backgroundManager = new BackgroundManager();
        this.worldRenderer = new WorldRenderer(
                backgroundManager,
                viewport,
                lawnMowerController,
                zombieWaveController);

        // ===== ECS Init (MỚI) =====
        renderSystem = new RenderSystem(batch);
        animationSystem = new AnimationSystem(); 
        sunSystem = new SunProductionSystem(this, entities);
        attackSystem = new PlantAttackSystem(this, zombieWaveController); 
        movementSystem = new MovementSystem();
        collisionSystem = new CollisionSystem(
                entities,
                zombieWaveController,
                plantGridController);
        sunPickupSystem = new SunPickupSystem(
                entities,
                camera,
                this);
    
        wallnutStateSystem = new WallnutStateSystem();
        explosionSystem = new ExplosionSystem(zombieWaveController, plantGridController);

        shapeRenderer = new ShapeRenderer();
    }

    // ================== Helper ==================

    private boolean isPlaying() { return gameState.isPlaying(); }
    private boolean isCountdown() { return gameState.isCountdown(); }
    private boolean isGameOver() { return gameState.isGameOver(); }
    public GameState getGameState() { return gameState; }

    // ================== HUD interaction ==================

    public void onPlantCardClicked(PlantCard card) {
        if (isGameOver()) return;
        // Logic cũ dùng plantPlacementController, nhưng giờ mình chuyển sang kéo thả trực tiếp ở dưới
        // Nếu bạn muốn click-to-place, cần viết lại 1 xíu logic ở đây.
        // Nhưng tạm thời ta tập trung vào kéo thả (Drag)
    }

    public void onPlantCardDragged(PlantCard card, float screenX, float screenY) {
        if (isGameOver()) return;
        if (!isPlaying()) return;

        // Logic đặt cây (chuyển từ PlantPlacementController sang đây cho gọn)
        Vector2 world = viewport.unproject(new Vector2(screenX, screenY));
        int[] cell = GridConfig.worldToNearestCell(world.x, world.y);
        int row = cell[0];
        int col = cell[1];

        if (row < 0 || col < 0 || !GridConfig.isInsideGrid(row, col)) return;

        int currentSun = hudController.getSunPoints();
        if (!card.canUse(currentSun)) return;

        boolean spawned = spawnPlantFromCardAtGrid(card, row, col);
        if (!spawned) return;

        if (!hudController.spendSun(card.type.cost)) return;

        card.triggerUse();
    }

    private PlantType toPlantType(ItemType itemType) {
        switch (itemType) {
            case SUNFLOWER: return PlantType.SUNFLOWER;
            case PEASHOOTER: return PlantType.PEASHOOTER;
            case WALLNUT: return PlantType.WALLNUT;
            case CHERRYBOMB: return PlantType.CHERRY_BOMB;
            case POTATOMINE: return PlantType.POTATO_MINE;
            case SNOWPEA: return PlantType.SNOW_PEA; 
            default: return PlantType.PEASHOOTER;
        }
    }

    private boolean spawnPlantFromCardAtGrid(PlantCard card, int row, int col) {
        if (plantGridController.isCellOccupied(row, col)) return false;
        PlantType plantType = toPlantType(card.type);
        Plant plant = PlantFactory.createPlantAtCell(plantType, col, row);
        if (plant == null) return false;
        entities.add(plant);
        plants.add(plant);
        plantGridController.registerPlantAtCell(plant, row, col);
        return true;
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

    private void updateGame(float delta) {
        if (!isPlaying()) return;
        zombieWaveController.update(delta);
        lawnMowerController.update(delta, zombieWaveController.getZombies());
    }

    private void handleEscape() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new ResumeScreen(game, this));
        }
    }

    @Override
    public void show() {
        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(hudStage);
        multiplexer.addProcessor(sunPickupSystem);
        Gdx.input.setInputProcessor(multiplexer);
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

        // 1. Vẽ Thế giới (Background...)
        batch.begin();
        worldRenderer.render(batch, isCountdown(), isPlaying());
        batch.end();

        // 2. Vẽ Lưới (Debug)
        drawDebugGrid();

        // 3. Update & Vẽ ECS (Cây, Đạn, Animation)
        // Đây là phần quan trọng để cây lắc lư và bắn đạn
        if (isPlaying()) {
            sunSystem.update(delta);
            wallnutStateSystem.update(entities);
            explosionSystem.update(entities, delta);
            
            animationSystem.update(entities, delta);
            attackSystem.update(plants, delta);
            movementSystem.update(entities, delta);
            collisionSystem.update(delta);
            sunPickupSystem.update(delta);

            batch.setProjectionMatrix(camera.combined);
            renderSystem.update(entities);
        }

        // 4. Vẽ HUD
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

    @Override
    public void dispose() {
        batch.dispose();
        hudStage.dispose();
        hudController.dispose();
        worldRenderer.dispose();
        shapeRenderer.dispose();
        pvz.com.entities.Zombies.ZombieSounds.disposeAll();
    }

    @Override public void pause() { }
    @Override public void resume() { }
    @Override public void hide() { }

    // ================== IGameSpawner ==================
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
    
    // ================== ISunReceiver & Helpers ==================
    @Override
    public void addSun(int amount) {
        hudController.addSun(amount);
    }

    public boolean spendSun(int cost) {
        return hudController.spendSun(cost);
    }

    public int getSunPoints() {
        return hudController.getSunPoints();
    }
}