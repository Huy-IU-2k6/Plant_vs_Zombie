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
import pvz.com.entities.components.PlantDamageType;
import pvz.com.entities.plants.Plant;
import pvz.com.entities.projectiles.FrozenPeaProjectile;
import pvz.com.entities.projectiles.PeaProjectile;
import pvz.com.entities.suns.Sun;
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
import pvz.com.systems.AnimationSystem;
import pvz.com.systems.CollisionSystem;
import pvz.com.systems.ExplosionSystem;
import pvz.com.systems.IGameSpawner;
import pvz.com.systems.ISunReceiver;
import pvz.com.systems.MovementSystem;
import pvz.com.systems.PlantAttackSystem;
import pvz.com.systems.RenderSystem;
import pvz.com.systems.SunPickupSystem;
import pvz.com.systems.SunProductionSystem;
import pvz.com.systems.WallnutStateSystem;

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
    private static final String GAME_BGM_PATH = "musics/Grasswalk.mp3";

    private Music inheritedMenuMusic; // nhạc menu nhận từ MainMenu
    private float inheritedMenuStartVolume = 1f;

    private Music gameMusic; // nhạc gameplay do GameScreen tự load
    private float crossfadeTimer = 0f;
    private boolean startedFade = false;

    // ===== Core refs =====
    private final Game game;
    private final SpriteBatch batch;

    private final Stage hudStage;
    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final ShapeRenderer shapeRenderer;

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
        this(game, null);
    }

    public GameScreen(Game game, Music inheritedMenuMusic) {
        this.game = game;
        this.inheritedMenuMusic = inheritedMenuMusic;

        this.batch = new SpriteBatch();

        // HUD stage
        this.hudStage = new Stage(new ScreenViewport());
        this.hudStage.getRoot().setUserObject(this);

        // World camera/viewport
        this.camera = new OrthographicCamera();
        this.viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        camera.position.set(WORLD_WIDTH / 2f, WORLD_HEIGHT / 2f, 0f);
        camera.update();

        // GridConfig init theo world size
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

        // Placement controller (bây giờ dùng cái này để đặt cây)
        this.plantPlacementController = new PlantPlacementController(
                viewport,
                hudController,
                plantGridController,
                gameWorld);

        // Shape debug
        this.shapeRenderer = new ShapeRenderer();

        // ===== ECS Init =====
        this.renderSystem = new RenderSystem(batch);
        this.animationSystem = new AnimationSystem();
        this.sunSystem = new SunProductionSystem(this, entities);
        this.attackSystem = new PlantAttackSystem(this, zombieWaveController);
        this.movementSystem = new MovementSystem();
        this.collisionSystem = new CollisionSystem(entities, zombieWaveController, plantGridController);
        this.sunPickupSystem = new SunPickupSystem(entities, camera, this);
        this.wallnutStateSystem = new WallnutStateSystem();
        this.explosionSystem = new ExplosionSystem(zombieWaveController, plantGridController);

        // ===== Load game music =====
        this.gameMusic = Gdx.audio.newMusic(Gdx.files.internal(GAME_BGM_PATH));
        this.gameMusic.setLooping(true);
        this.gameMusic.setVolume(0f); // sẽ fade in
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
        if (card == null)
            return;
        if (isGameOver())
            return;

        // Mode click-to-place (nếu muốn dùng lại) — hiện giờ chỉ trừ sun + cooldown
        plantPlacementController.handleCardClicked(card, isPlaying());
    }

    public void onPlantCardDragged(PlantCard card, float screenX, float screenY) {
        if (card == null)
            return;
        if (isGameOver())
            return;

        // Quan trọng: dùng PlacementController (vì GridController không còn
        // placePlantFromDrag)
        plantPlacementController.handleCardDragged(card, screenX, screenY, isPlaying());
    }

    // ================== AUDIO ==================

    private void startCrossfade() {
        if (startedFade)
            return;
        startedFade = true;

        // đảm bảo game music chạy
        if (gameMusic != null && !gameMusic.isPlaying()) {
            gameMusic.play();
        }

        // nếu nhạc menu có sẵn thì đảm bảo nó đang play
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

    // ================== Game State & Loop ==================

    private void updateState(float delta) {
        if (isGameOver())
            return;
        if (!isCountdown())
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
        if (!isPlaying())
            return;
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
        multiplexer.addProcessor(sunPickupSystem);
        Gdx.input.setInputProcessor(multiplexer);

        // Nếu quay lại từ ResumeScreen, đảm bảo music chạy lại
        if (gameMusic != null && startedFade && !gameMusic.isPlaying()) {
            gameMusic.play();
        }

        // Lần đầu vào thì crossfade
        startCrossfade();
    }

    @Override
    public void render(float delta) {
        updateCrossfade(delta);

        updateState(delta);
        updateWorldControllers(delta);
        handleEscape();

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        batch.setProjectionMatrix(camera.combined);

        // 1) World
        batch.begin();
        worldRenderer.render(batch, isCountdown(), isPlaying());
        batch.end();

        drawDebugGrid();

        // 2) ECS
        if (isPlaying()) {
            sunSystem.update(delta);
            wallnutStateSystem.update(entities);
            explosionSystem.update(entities, delta);

            animationSystem.update(entities, delta);
            attackSystem.update(plants, delta);
            movementSystem.update(entities, delta);
            collisionSystem.update(delta);
            sunPickupSystem.update(delta);

            gameWorld.update(delta);

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
        Gdx.gl.glDisable(GL20.GL_BLEND);
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
        /* show() sẽ xử lý play lại */ }

    @Override
    public void hide() {
        // Không dispose ở đây để tránh mất nhạc khi chuyển sang ResumeScreen
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

    // ================== IGameSpawner ==================

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

    // ================== ISunReceiver ==================

    @Override
    public void addSun(int amount) {
        hudController.addSun(amount);
    }
}
