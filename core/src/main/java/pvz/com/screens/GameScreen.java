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
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.ArrayList;
import java.util.List;

import pvz.com.entities.Entity;
import pvz.com.entities.plants.Plant;
import pvz.com.items.PlantCard;
import pvz.com.logic.GameWorld;
import pvz.com.logic.HudController;
import pvz.com.logic.LawnMowerController;
import pvz.com.logic.PlantGridController;
import pvz.com.logic.PlantPlacementController;
import pvz.com.logic.WorldRenderer;
import pvz.com.logic.ZombieWaveController;
import pvz.com.managers.BackgroundManager;
import pvz.com.managers.GridConfig;

public class GameScreen implements Screen {

    // ===== World & layout =====
    private static final float WORLD_WIDTH = 800f;
    private static final float WORLD_HEIGHT = 600f;

    // ===== Game config =====
    private static final float COUNTDOWN_DURATION = 6f;
    private static final int INITIAL_SUN = 150;

    private ShapeRenderer shapeRenderer;

    // Độ dài 1 màn zombie (phải match với ZombieWaveController nếu dùng
    // levelDuration)
    private static final float LEVEL_DURATION = 240f; // 4 phút

    // ===== Zombie lane config (dựa trên GridConfig) =====
    private static final int ZOMBIE_LANE_COUNT = GridConfig.ROWS;
    private static final float ZOMBIE_START_OFFSET_X = 200f;

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

    // ===== ECS data (được GameWorld sử dụng) =====
    private final List<Entity> entities = new ArrayList<>();
    private final List<Plant> plants = new ArrayList<>();

    // ===== Controllers =====
    private final HudController hudController;
    private final PlantGridController plantGridController;
    private final LawnMowerController lawnMowerController;
    private final ZombieWaveController zombieWaveController;
    private final WorldRenderer worldRenderer;

    // ===== World / logic tách riêng =====
    private final GameWorld gameWorld;
    private final PlantPlacementController plantPlacementController;

    private State state = State.COUNTDOWN;

    public GameScreen(Game game) {
        this.game = game;

        // --- core ---
        this.batch = new SpriteBatch();

        // HUD stage (UI)
        this.hudStage = new Stage(new ScreenViewport());

        hudStage.setDebugAll(true);

        // để PlantCard lấy được GameScreen từ Stage (userObject)
        this.hudStage.getRoot().setUserObject(this);

        // Camera + viewport world 800x600
        this.camera = new OrthographicCamera();
        this.viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        camera.position.set(WORLD_WIDTH / 2f, WORLD_HEIGHT / 2f, 0f);
        camera.update();

        // ===== Controllers =====
        this.hudController = new HudController(hudStage, COUNTDOWN_DURATION, INITIAL_SUN);

        // PlantGridController dùng cùng list entities/plants với GameWorld
        this.plantGridController = new PlantGridController(entities, plants, camera);
        plantGridController.setEnabled(false);

        this.lawnMowerController = new LawnMowerController(
                ZOMBIE_LANE_COUNT,
                WORLD_WIDTH,
                180f // khoảng cách mower spawn từ mép trái
        );

        this.zombieWaveController = new ZombieWaveController(
                WORLD_WIDTH,
                ZOMBIE_LANE_COUNT,
                ZOMBIE_START_OFFSET_X,
                90, // tổng số zombie của màn (tuỳ bạn tune)
                LEVEL_DURATION);

        BackgroundManager backgroundManager = new BackgroundManager();
        this.worldRenderer = new WorldRenderer(
                backgroundManager,
                viewport,
                lawnMowerController,
                zombieWaveController);

        // GameWorld: chứa toàn bộ ECS (systems, spawn, pickup sun...)
        this.gameWorld = new GameWorld(
                hudController,
                entities,
                plants,
                camera,
                zombieWaveController,
                plantGridController,
                batch);

        // Controller đặt cây bằng card (click/drag)
        this.plantPlacementController = new PlantPlacementController(
                viewport,
                hudController,
                plantGridController,
                gameWorld);

        shapeRenderer = new ShapeRenderer();
    }

    // ================== Helper ==================

    private boolean isPlaying() {
        return state == State.PLAYING;
    }

    private boolean isCountdown() {
        return state == State.COUNTDOWN;
    }

    // ================== HUD interaction ==================

    /**
     * Được PlantCard gọi khi người chơi click 1 card (mode click-to-place).
     * Stage HUD sẽ lấy GameScreen qua getRoot().getUserObject().
     */
    public void onPlantCardClicked(PlantCard card) {
        plantPlacementController.handleCardClicked(card, isPlaying());
    }

    /**
     * Được PlantCard gọi khi người chơi kéo card và thả ra màn hình.
     */
    public void onPlantCardDragged(PlantCard card, float screenX, float screenY) {
        plantPlacementController.handleCardDragged(card, screenX, screenY, isPlaying());
    }

    // ================== Game state ==================

    private void updateState(float delta) {
        if (!isCountdown()) {
            return;
        }

        if (hudController.isCountdownFinished()) {
            state = State.PLAYING;

            hudController.onCountdownFinished();

            zombieWaveController.startWave();
            lawnMowerController.createLawnMowers();
            plantGridController.setEnabled(true);
        }
    }

    private void updateGame(float delta) {
        if (!isPlaying()) {
            return;
        }

        // logic thuần "thế giới" (zombie, mower, wave...)
        zombieWaveController.update(delta);
        lawnMowerController.update(delta, zombieWaveController.getZombies());
        // TODO: check va chạm plant, bullet... nếu zombie cũng dùng ECS
    }

    public GameWorld getGameWorld() {
        return gameWorld;
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
        // - System xử lý click nhặt Sun (SunPickupSystem)
        InputMultiplexer multiplexer = new InputMultiplexer();

        // Ưu tiên HUD trước để click vào card không bị lọt xuống world
        multiplexer.addProcessor(hudStage);

        // SunPickupSystem (InputProcessor) lấy từ GameWorld
        multiplexer.addProcessor(gameWorld.getSunPickupSystem());

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

        // --- world (background, zombie, mower, HUD countdown, ...) ---
        batch.begin();
        worldRenderer.render(batch, isCountdown(), isPlaying());
        batch.end();

        drawDebugGrid();

        // --- ECS (plants, projectiles, sun system, attack system) ---
        gameWorld.update(delta, isPlaying());
        if (isPlaying()) {
            batch.setProjectionMatrix(camera.combined);
            gameWorld.render(batch);
        }

        // --- HUD (SeedBank, countdown, card, sun text...) ---
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
        shapeRenderer.dispose();
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

    private void drawDebugGrid() {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        for (int row = 0; row < GridConfig.ROWS; row++) {
            for (int col = 0; col < GridConfig.COLS; col++) {
                float x = GridConfig.getCellOriginX(col);
                float y = GridConfig.getCellOriginY(row);

                // viền ô
                shapeRenderer.rect(x, y, GridConfig.CELL_WIDTH, GridConfig.CELL_HEIGHT);

                // tâm ô
                float cx = GridConfig.getCellCenterX(col);
                float cy = GridConfig.getCellCenterY(row);
                float r = 3f;
                shapeRenderer.circle(cx, cy, r);
            }
        }

        shapeRenderer.end();
    }

}
