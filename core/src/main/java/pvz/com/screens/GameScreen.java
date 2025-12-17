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
import pvz.com.items.PlantCard;
import pvz.com.logic.GameState;
import pvz.com.logic.GameWorld;
import pvz.com.logic.HudController;
import pvz.com.logic.LawnMowerController;
import pvz.com.logic.PlantGridController;
import pvz.com.logic.PlantPlacementController;
import pvz.com.logic.ShovelController;
import pvz.com.logic.WorldRenderer;
import pvz.com.logic.ZombieWaveController;
import pvz.com.managers.BackgroundManager;
import pvz.com.managers.DesignConfig;
import pvz.com.managers.GridConfig;
import pvz.com.systems.RenderSystem;

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

    private final Game game;

    // ===== Render =====
    private final SpriteBatch batch;
    private final ShapeRenderer shapeRenderer;

    // ===== Camera/viewport =====
    private final OrthographicCamera camera;
    private final Viewport viewport;

    // ===== ECS Data =====
    private final List<Entity> entities = new ArrayList<>();
    private final List<Plant> plants = new ArrayList<>();

    // ===== Game State =====
    private final GameState gameState;

    // ===== Controllers =====
    private final PlantGridController plantGridController;
    private final ShovelController shovelController;
    private final LawnMowerController lawnMowerController;
    private final ZombieWaveController zombieWaveController;
    private final WorldRenderer worldRenderer;

    // ===== HUD =====
    private final Stage hudStage;
    private final HudController hudController;

    // ===== World wrapper (ECS update + win/lose) =====
    private final GameWorld gameWorld;

    // ===== Placement =====
    private final PlantPlacementController plantPlacementController;

    // ===== Render System (chỉ render entities) =====
    private final RenderSystem renderSystem;

    // ===== Overlay switch guard =====
    private boolean pushedEndScreen = false;

    // ===== Music =====
    private Music inheritedMenuMusic;
    private float inheritedMenuStartVolume = 1f;

    private Music gameMusic; // nếu bạn có game BGM riêng thì set vào đây
    private float crossfadeTimer = 0f;
    private boolean startedFade = false;

    public GameScreen(Game game) {
        this(game, null);
    }

    /** inheritedMenuMusic: truyền từ MainMenuScreen để kế thừa nhạc */
    public GameScreen(Game game, Music inheritedMenuMusic) {
        this.game = game;
        this.inheritedMenuMusic = inheritedMenuMusic;

        this.batch = new SpriteBatch();
        this.shapeRenderer = new ShapeRenderer();

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

        // State
        this.gameState = new GameState();

        // Grid controller (ban đầu disable cho đến khi countdown xong)
        this.plantGridController = new PlantGridController(entities, plants, camera);
        this.plantGridController.setEnabled(false);

        // Shovel controller (HUD cần)
        this.shovelController = new ShovelController(plantGridController);

        // HUD (NEW ctor: cần PlantGridController + ShovelController)
        this.hudController = new HudController(
                hudStage,
                COUNTDOWN_DURATION,
                INITIAL_SUN,
                plantGridController,
                shovelController);

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

        // GameWorld (ECS update + check win/lose) — dùng bản mới
        this.gameWorld = new GameWorld(
                gameState,
                hudController,
                entities,
                plants,
                camera,
                zombieWaveController,
                plantGridController);

        // Placement controller
        this.plantPlacementController = new PlantPlacementController(
                viewport,
                hudController,
                plantGridController,
                gameWorld);

        // Entity renderer
        this.renderSystem = new RenderSystem(batch);

        // Nếu có gameMusic riêng thì set + crossfade ở đây (tuỳ project bạn)
        // startCrossfadeToGameMusicIfNeeded();
    }

    // ================== Public getters ==================
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

    private void clearScreen() {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
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

    private void renderWorldOnly(boolean isCountdown, boolean isPlaying) {
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        worldRenderer.render(batch, isCountdown, isPlaying);
        batch.end();
    }

    /**
     * Render snapshot frame (không update logic) - dùng cho ResumeScreen nếu cần
     */
    public void renderFrozen() {
        clearScreen();

        // background + lanes + mowers + zombies
        renderWorldOnly(gameState.isCountdown(), true);

        // entities (plants/projectiles/suns...)
        batch.setProjectionMatrix(camera.combined);
        renderSystem.update(entities);

        // HUD
        hudStage.act(0f);
        hudStage.draw();
    }

    // ================== Plant card interaction ==================
    public void onPlantCardClicked(PlantCard card) {
        if (card == null || gameState.isGameOver())
            return;
        plantPlacementController.handleCardClicked(card, gameState.isPlaying());
    }

    public void onPlantCardDragged(PlantCard card, float screenX, float screenY) {
        if (card == null || gameState.isGameOver())
            return;
        plantPlacementController.handleCardDragged(card, screenX, screenY, gameState.isPlaying());
    }

    // ================== AUDIO ==================
    private void updateCrossfade(float delta) {
        if (!startedFade || crossfadeTimer <= 0f)
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

        // Chỉ xử lý chuyển state khi đang COUNTDOWN
        if (!gameState.isCountdown())
            return;

        if (hudController.isCountdownFinished()) {
            gameState.setState(GameState.State.PLAYING);

            // HUD mở seedbank + show shovel (logic nằm ở HudController mới)
            hudController.onCountdownFinished();

            // start wave + lawn mowers + enable grid
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
            pauseMusics();
            game.setScreen(new ResumeScreen(game, this));
        }
    }

    private void pushEndOverlayIfNeeded() {
        if (!gameState.isGameOver() || pushedEndScreen)
            return;
        pushedEndScreen = true;

        if (gameState.isPlayerWon()) {
            game.setScreen(new GameWinScreen(game, this));
        } else {
            game.setScreen(new GameOverScreen(game, this, false));
        }
    }

    // ================== Screen ==================
    @Override
    public void show() {
        InputMultiplexer multiplexer = new InputMultiplexer();

        // UI
        multiplexer.addProcessor(hudStage);

        // Sun pickup (đúng theo GameWorld mới)
        multiplexer.addProcessor(gameWorld.getSunPickupSystem());

        // (tuỳ bạn) nếu PlantGridController extends InputAdapter và bạn cần bắt click
        // grid trực tiếp:
        // multiplexer.addProcessor(plantGridController);

        Gdx.input.setInputProcessor(multiplexer);

        // Nếu muốn bắt đầu crossfade khi vào màn:
        // startCrossfadeToGameMusicIfNeeded();
    }

    @Override
    public void render(float delta) {
        updateCrossfade(delta);
        handleEscape();

        updateState(delta);
        updateWorldControllers(delta);

        clearScreen();

        // world base
        renderWorldOnly(gameState.isCountdown(), gameState.isPlaying());

        // debug grid
        drawDebugGrid();

        // ECS update + render entities
        if (isPlaying()) {
            gameWorld.update(delta);

            batch.setProjectionMatrix(camera.combined);
            renderSystem.update(entities);
        }

        // HUD
        hudStage.act(delta);
        hudStage.draw();

        // End overlay
        pushEndOverlayIfNeeded();
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
    }

    @Override
    public void hide() {
        pauseMusics();
    }

    @Override
    public void dispose() {
        stopAndDisposeMusics();

        batch.dispose();
        shapeRenderer.dispose();

        hudStage.dispose();
        hudController.dispose();

        worldRenderer.dispose();

        // nếu project bạn có ZombieSounds static:
        pvz.com.entities.Zombies.ZombieSounds.disposeAll();
    }
}
