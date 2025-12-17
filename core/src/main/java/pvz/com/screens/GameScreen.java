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

    // NOTE: nếu project có path khác thì đổi tại đây
    private static final String GAME_BGM_PATH = "audio/game_bgm.mp3";

    private final Game game;
    private final SpriteBatch batch;

    // ===== Camera/viewport =====
    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final ShapeRenderer shapeRenderer;

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

    // ===== World wrapper (ECS update + win/lose) =====
    private final GameWorld gameWorld;

    // ===== Placement =====
    private final PlantPlacementController plantPlacementController;

    // ===== Overlay switch guard =====
    private boolean pushedEndScreen = false;

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
                DesignConfig.START_X - 80f
        );

        // Zombie waves
        this.zombieWaveController = new ZombieWaveController(
                WORLD_WIDTH,
                WORLD_HEIGHT,
                ZOMBIE_START_OFFSET_X,
                90,
                LEVEL_DURATION
        );

        // World renderer
        BackgroundManager backgroundManager = new BackgroundManager();
        this.worldRenderer = new WorldRenderer(
                backgroundManager,
                viewport,
                lawnMowerController,
                zombieWaveController
        );

        // GameWorld: giữ RenderSystem nội bộ, nên truyền batch vào đây
        this.gameWorld = new GameWorld(
                gameState,
                hudController,
                entities,
                plants,
                camera,
                zombieWaveController,
                plantGridController,
                batch
        );

        // Placement controller
        this.plantPlacementController = new PlantPlacementController(
                viewport,
                hudController,
                plantGridController,
                gameWorld
        );

        // Debug renderer
        this.shapeRenderer = new ShapeRenderer();
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
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void renderWorldOnly(boolean isCountdown, boolean isPlaying) {
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        worldRenderer.render(batch, isCountdown, isPlaying);
        batch.end();
    }

    // Render snapshot frame (không update logic)
    public void renderFrozen() {
        clearScreen();

        // background + lanes + mowers + zombies render base
        renderWorldOnly(gameState.isCountdown(), true);

        // entities (plants/projectiles/suns...) -> dùng GameWorld.render()
        batch.setProjectionMatrix(camera.combined);
        gameWorld.render(batch);

        // HUD
        hudStage.act(0f);
        hudStage.draw();
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

    private void initMusicIfNeeded() {
        if (gameMusic == null) {
            gameMusic = Gdx.audio.newMusic(Gdx.files.internal(GAME_BGM_PATH));
            gameMusic.setLooping(true);
            gameMusic.setVolume(0f); // sẽ fade-in
        }
    }

    private void startCrossfadeIfNeeded() {
        if (startedFade) return;

        initMusicIfNeeded();
        if (gameMusic != null && !gameMusic.isPlaying()) {
            gameMusic.play();
        }

        // có inheritedMenuMusic thì fade-out nó
        if (inheritedMenuMusic != null) {
            inheritedMenuStartVolume = inheritedMenuMusic.getVolume();
        }

        crossfadeTimer = CROSSFADE_DURATION;
        startedFade = true;
    }

    private void updateCrossfade(float delta) {
        if (!startedFade || crossfadeTimer <= 0f) return;

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

    private void pushEndOverlayIfNeeded() {
        if (!gameState.isGameOver() || pushedEndScreen) return;
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
        // giữ cơ chế inherited music: vào game thì bắt đầu crossfade
        startCrossfadeIfNeeded();

        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(hudStage);

        // input cho grid controller (shovel/remove khi enabled)
        multiplexer.addProcessor(plantGridController);

        // Sun pickup input
        multiplexer.addProcessor(gameWorld.getSunPickupSystem());

        Gdx.input.setInputProcessor(multiplexer);
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
        drawDebugGrid();

        // ECS update + render entities
        if (isPlaying()) {
            gameWorld.update(delta);

            batch.setProjectionMatrix(camera.combined);
            gameWorld.render(batch);
        }

        hudStage.act(delta);
        hudStage.draw();

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
        hudStage.dispose();
        hudController.dispose();
        worldRenderer.dispose();
        shapeRenderer.dispose();
        pvz.com.entities.Zombies.ZombieSounds.disposeAll();
    }
}
