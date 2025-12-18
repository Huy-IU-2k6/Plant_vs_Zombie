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

    private static final float WORLD_WIDTH = DesignConfig.BASE_SCREEN_W;
    private static final float WORLD_HEIGHT = DesignConfig.BASE_SCREEN_H;

    private static final float COUNTDOWN_DURATION = 6f;
    private static final int INITIAL_SUN = 150;
    private static final float LEVEL_DURATION = 240f;

    private static final float ZOMBIE_START_OFFSET_X = 200f;

    private static final float CROSSFADE_DURATION = 0.8f;
    private static final float GAME_BGM_VOLUME = 1f;

    private final Game game;

    private final SpriteBatch batch;
    private final ShapeRenderer shapeRenderer;

    private final OrthographicCamera camera;
    private final Viewport viewport;

    private final List<Entity> entities = new ArrayList<>();
    private final List<Plant> plants = new ArrayList<>();

    private final GameState gameState;

    private final PlantGridController plantGridController;
    private final ShovelController shovelController;
    private final LawnMowerController lawnMowerController;
    private final ZombieWaveController zombieWaveController;
    private final WorldRenderer worldRenderer;

    private final Stage hudStage;
    private final HudController hudController;

    private final GameWorld gameWorld;

    private final PlantPlacementController plantPlacementController;

    private final RenderSystem renderSystem;

    private boolean pushedEndScreen = false;

    private Music inheritedMenuMusic;
    private final float inheritedMenuStartVolume = 1f;

    private Music gameMusic;
    private float crossfadeTimer = 0f;
    private final boolean startedFade = false;

    public GameScreen(Game game) {
        this(game, null);
    }

    public GameScreen(Game game, Music inheritedMenuMusic) {
        this.game = game;
        this.inheritedMenuMusic = inheritedMenuMusic;
        this.gameMusic = Gdx.audio.newMusic(Gdx.files.internal("musics/Grasswalk.mp3"));
        this.gameMusic.setLooping(true);
        this.gameMusic.setVolume(GAME_BGM_VOLUME);
        this.gameMusic.play();

        this.batch = new SpriteBatch();
        this.shapeRenderer = new ShapeRenderer();

        this.hudStage = new Stage(new ScreenViewport());

        this.camera = new OrthographicCamera();
        this.viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        camera.position.set(WORLD_WIDTH / 2f, WORLD_HEIGHT / 2f, 0f);
        camera.update();

        GridConfig.init(viewport.getWorldWidth(), viewport.getWorldHeight());

        this.gameState = new GameState();

        this.plantGridController = new PlantGridController(entities, plants, camera);
        this.plantGridController.setEnabled(false);

        this.shovelController = new ShovelController(plantGridController);

        this.hudController = new HudController(
                hudStage,
                COUNTDOWN_DURATION,
                INITIAL_SUN,
                plantGridController,
                shovelController);

        this.lawnMowerController = new LawnMowerController(
                WORLD_WIDTH - 50f,
                DesignConfig.START_X - 80f);

        this.zombieWaveController = new ZombieWaveController(
                WORLD_WIDTH,
                WORLD_HEIGHT,
                ZOMBIE_START_OFFSET_X,
                90,
                LEVEL_DURATION);

        BackgroundManager backgroundManager = new BackgroundManager();
        this.worldRenderer = new WorldRenderer(
                backgroundManager,
                viewport,
                lawnMowerController,
                zombieWaveController);

        this.gameWorld = new GameWorld(
                gameState,
                hudController,
                entities,
                plants,
                camera,
                zombieWaveController,
                plantGridController);

        this.plantPlacementController = new PlantPlacementController(
                viewport,
                hudController,
                plantGridController,
                gameWorld);

        this.renderSystem = new RenderSystem(batch);
    }

    public GameState getGameState() {
        return gameState;
    }

    public HudController getHudController() {
        return hudController;
    }

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

    public void renderFrozen() {
        clearScreen();

        
        renderWorldOnly(gameState.isCountdown(), true);

        
        batch.setProjectionMatrix(camera.combined);
        renderSystem.update(entities);

        
        hudStage.act(0f);
        hudStage.draw();
    }

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

    @Override
    public void show() {
        InputMultiplexer multiplexer = new InputMultiplexer();

        multiplexer.addProcessor(hudStage);
        this.hudStage.getRoot().setUserObject(this);

        multiplexer.addProcessor(gameWorld.getSunPickupSystem());

        Gdx.input.setInputProcessor(multiplexer);
        if (gameMusic == null) {
    gameMusic = Gdx.audio.newMusic(Gdx.files.internal("music/Grasswalk.mp3")); 
    gameMusic.setLooping(true);
    gameMusic.setVolume(GAME_BGM_VOLUME);
}
if (!gameMusic.isPlaying()) {
    gameMusic.play();
}
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
            gameWorld.update(delta);

            batch.setProjectionMatrix(camera.combined);
            renderSystem.update(entities);
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
        if (gameMusic != null && !gameMusic.isPlaying()) {
        gameMusic.play();
    }
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

        pvz.com.entities.Zombies.ZombieSounds.disposeAll();
    }
}
