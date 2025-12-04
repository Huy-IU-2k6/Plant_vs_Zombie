package pvz.com.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
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
import pvz.com.items.ItemType;
import pvz.com.entities.plants.Plant;
import pvz.com.entities.plants.PlantType;
import pvz.com.factories.PlantFactory;
import pvz.com.entities.suns.Sun;
import pvz.com.entities.components.PlantDamageType;
import pvz.com.entities.projectiles.FrozenPeaProjectile;
import pvz.com.entities.projectiles.PeaProjectile;

import pvz.com.systems.IGameSpawner;
import pvz.com.systems.RenderSystem;
import pvz.com.systems.SunProductionSystem;
import pvz.com.systems.PlantAttackSystem;
import pvz.com.systems.MovementSystem;
import pvz.com.systems.CollisionSystem;
import pvz.com.systems.SunPickupSystem;

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

    // Độ dài 1 màn zombie (phải match với ZombieWaveController nếu bạn dùng
    // overload có levelDuration)
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

    // ===== ECS: plants, projectiles, systems =====
    private final List<Entity> entities = new ArrayList<>();
    private final List<Plant> plants = new ArrayList<>();

    private final RenderSystem renderSystem;
    private final SunProductionSystem sunSystem;
    private final PlantAttackSystem attackSystem;
    private final MovementSystem movementSystem;
    private final CollisionSystem collisionSystem;
    private final SunPickupSystem sunPickupSystem;

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

        lawnMowerController = new LawnMowerController(
                ZOMBIE_LANE_COUNT,
                WORLD_WIDTH,
                180f // khoảng cách mower spawn từ mép trái
        );

        // ZombieWaveController mới: tự quản lý levelDuration, nhiều loại zombie, tăng
        // khó theo time
        zombieWaveController = new ZombieWaveController(
                WORLD_WIDTH,
                ZOMBIE_LANE_COUNT,
                ZOMBIE_START_OFFSET_X,
                90, // tổng số zombie của màn (tuỳ bạn tune)
                LEVEL_DURATION // thời lượng màn
        );

        BackgroundManager backgroundManager = new BackgroundManager();
        worldRenderer = new WorldRenderer(
                backgroundManager,
                viewport,
                lawnMowerController,
                zombieWaveController);

        // ===== ECS init =====
        renderSystem = new RenderSystem(batch);
        sunSystem = new SunProductionSystem(this, entities); // Sunflower sinh sun
        attackSystem = new PlantAttackSystem(this); // Plant bắn đạn
        movementSystem = new MovementSystem(); // Đạn / entity di chuyển
        collisionSystem = new CollisionSystem(
                entities,
                zombieWaveController,
                plantGridController); // Đạn trúng zombie
        sunPickupSystem = new SunPickupSystem(
                entities,
                camera,
                this); // Click nhặt sun + timeout
    }

    // ================== HUD interaction ==================

    /** Được PlantCard gọi khi người chơi click 1 card. */
    public void onPlantCardClicked(PlantCard card) {
        // Chỉ xử lý sun cho card click (nếu bạn còn dùng mode click-to-place)
        if (!hudController.spendSun(card.type.cost))
            return;

        card.triggerUse();
    }

    /** Được PlantCard gọi khi người chơi kéo card và thả ra màn hình. */
    public void onPlantCardDragged(PlantCard card, float screenX, float screenY) {
        if (state != State.PLAYING)
            return;

        // 1) screen -> world
        Vector2 world = viewport.unproject(new Vector2(screenX, screenY));

        // 2) world -> grid (snap về ô gần nhất)
        int[] cell = GridConfig.worldToNearestCell(world.x, world.y);
        int row = cell[0];
        int col = cell[1];

        if (row < 0 || col < 0 || !GridConfig.isInsideGrid(row, col)) {
            // thả ra ngoài lawn thì bỏ
            return;
        }

        // 3) check đủ điều kiện (sun, cooldown, lock)
        int currentSun = hudController.getSunPoints();
        if (!card.canUse(currentSun)) {
            return;
        }

        // 4) cố gắng spawn cây trước
        boolean spawned = spawnPlantFromCardAtGrid(card, row, col);
        if (!spawned) {
            // ví dụ ô đang bị chiếm hoặc PlantFactory lỗi → không trừ sun
            return;
        }

        // 5) cây đã spawn thành công → trừ sun
        if (!hudController.spendSun(card.type.cost)) {
            // về lý thuyết không nên vào được nhánh này (vì đã canUse),
            // nhưng vẫn check an toàn
            return;
        }

        // 6) bật cooldown card
        card.triggerUse();
    }

    private PlantType toPlantType(ItemType itemType) {
        switch (itemType) {
            case SUNFLOWER:
                return PlantType.SUNFLOWER;
            case PEASHOOTER:
                return PlantType.PEASHOOTER;
            case WALLNUT:
                return PlantType.WALLNUT;

            case CHERRYBOMB:
                return PlantType.CHERRY_BOMB;
            case POTATOMINE:
                return PlantType.POTATO_MINE;

            // mấy thằng này chưa có PlantType tương ứng
            case CHOMPER:
            case REPEATER:
            case SNOWPEA:
            default:
                // tạm map về PEASHOOTER cho đỡ crash, sau này làm riêng
                return PlantType.PEASHOOTER;
        }
    }

    private boolean spawnPlantFromCardAtGrid(PlantCard card, int row, int col) {
        if (plantGridController.isCellOccupied(row, col)) {
            // Ô này đã có cây -> không trồng chồng
            return false;
        }

        PlantType plantType = toPlantType(card.type);
        Plant plant = PlantFactory.createPlantAtCell(plantType, col, row);
        if (plant == null) {
            // phòng trường hợp PlantFactory lỗi
            return false;
        }

        entities.add(plant);
        plants.add(plant);
        plantGridController.registerPlantAtCell(plant, row, col);
        return true;
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

        // logic thuần "thế giới" (zombie, mower, wave...)
        zombieWaveController.update(delta);
        lawnMowerController.update(delta, zombieWaveController.getZombies());
        // TODO: check va chạm plant, bullet... nếu dùng thêm ECS cho zombie
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
        // - Click xuống world để đặt cây theo grid (hiện tại dùng drag từ PlantCard)
        // - Click vào Sun rớt để nhặt
        InputMultiplexer multiplexer = new InputMultiplexer();

        // Ưu tiên HUD trước để click vào card không bị lọt xuống world
        multiplexer.addProcessor(hudStage);

        // System xử lý click nhặt Sun
        multiplexer.addProcessor(sunPickupSystem);

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
        boolean isCountdown = (state == State.COUNTDOWN);
        boolean isPlaying = (state == State.PLAYING);
        worldRenderer.render(batch, isCountdown, isPlaying);
        batch.end();

        // --- ECS (plants, projectiles, sun system, attack system) ---
        if (state == State.PLAYING) {
            // update hệ thống logic ECS
            sunSystem.update(delta); // sunflower sinh sun
            attackSystem.update(plants, delta); // plant bắn đạn (spawn PeaProjectile)

            movementSystem.update(entities, delta); // entity có MovementComponent di chuyển
            collisionSystem.update(delta); // đạn đâm zombie
            sunPickupSystem.update(delta); // sun tự biến mất nếu quá lâu

            // render tất cả entity ECS (plant, đạn, sun...)
            batch.setProjectionMatrix(camera.combined);
            renderSystem.update(entities);
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
        // Sun được spawn ra world, người chơi phải click để nhặt
        Sun sun = new Sun(x, y, amount);
        entities.add(sun);
    }

    @Override
    public void spawnProjectile(float x, float y, int damage,
            PlantDamageType type, Class<?> projectileClass) {
        if (projectileClass == PeaProjectile.class) {
            Entity pea = new PeaProjectile(x, y, damage);
            entities.add(pea);
        }
        else if (projectileClass == FrozenPeaProjectile.class) {
        Entity frozenPea = new FrozenPeaProjectile(x, y, damage);
        entities.add(frozenPea);
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
