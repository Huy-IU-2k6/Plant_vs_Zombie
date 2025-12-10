package pvz.com.logic;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.List;

import pvz.com.entities.Entity;
import pvz.com.entities.components.PlantDamageType;
import pvz.com.entities.plants.Plant;
import pvz.com.entities.projectiles.PeaProjectile;
import pvz.com.entities.suns.Sun;
import pvz.com.systems.CleanupSystem;
import pvz.com.systems.CollisionSystem;
import pvz.com.systems.IGameSpawner;
import pvz.com.systems.MovementSystem;
import pvz.com.systems.PlantAttackSystem;
import pvz.com.systems.RenderSystem;
import pvz.com.systems.SunPickupSystem;
import pvz.com.systems.SunProductionSystem;

public class GameWorld implements IGameSpawner {

    // Dùng chung list với GameScreen / PlantGridController
    private final List<Entity> entities;
    private final List<Plant> plants;

    // Trạng thái game dùng chung (COUNTDOWN / PLAYING / GAME_OVER)
    private final GameState gameState;

    // ECS systems
    private final RenderSystem renderSystem;
    private final SunProductionSystem sunSystem;
    private final PlantAttackSystem attackSystem;
    private final MovementSystem movementSystem;
    private final CollisionSystem collisionSystem;
    private final SunPickupSystem sunPickupSystem;
    private final CleanupSystem cleanupSystem;

    private final HudController hudController;

    public GameWorld(GameState gameState,
            HudController hudController,
            List<Entity> entities,
            List<Plant> plants,
            OrthographicCamera camera,
            ZombieWaveController zombieWaveController,
            PlantGridController plantGridController,
            SpriteBatch batch) {

        this.gameState = gameState;
        this.entities = entities;
        this.plants = plants;
        this.hudController = hudController;

        // Systems
        this.renderSystem = new RenderSystem(batch);
        this.sunSystem = new SunProductionSystem(this, entities); // IGameSpawner
        this.attackSystem = new PlantAttackSystem(this); // IGameSpawner
        this.movementSystem = new MovementSystem();
        this.collisionSystem = new CollisionSystem(
                entities,
                zombieWaveController,
                plantGridController);
        // SunPickupSystem giờ nhận GameWorld
        this.sunPickupSystem = new SunPickupSystem(entities, camera, this);
        this.cleanupSystem = new CleanupSystem(entities);
    }

    // ====== UPDATE / RENDER ======

    /**
     * Update ECS world (plants, projectiles, sun, v.v...).
     * Chỉ chạy khi gameState đang PLAYING.
     */
    public void update(float delta) {
        // Nếu không phải PLAYING (COUNTDOWN hoặc GAME_OVER) thì không update ECS
        if (!gameState.isPlaying()) {
            return;
        }

        // Giữ đúng thứ tự như GameScreen cũ
        sunSystem.update(delta); // sunflower sinh sun
        attackSystem.update(plants, delta); // plant bắn đạn (spawn PeaProjectile)
        movementSystem.update(entities, delta); // entity có MovementComponent di chuyển
        collisionSystem.update(delta); // đạn đâm zombie
        sunPickupSystem.update(delta); // sun tự biến mất nếu quá lâu
        cleanupSystem.update(); // xoá entity chết
    }

    public void render(SpriteBatch batch) {
        // batch ở đây không cần dùng vì RenderSystem đã giữ batch rồi
        renderSystem.update(entities);
    }

    public SunPickupSystem getSunPickupSystem() {
        return sunPickupSystem;
    }

    // Cho chỗ khác thêm plant vào world
    public void addPlant(Plant plant) {
        entities.add(plant);
        plants.add(plant);
    }

    public List<Entity> getEntities() {
        return entities;
    }

    public List<Plant> getPlants() {
        return plants;
    }

    public GameState getGameState() {
        return gameState;
    }

    // ====== IGameSpawner implementation ======

    @Override
    public void spawnSun(float x, float y, int amount) {
        Sun sun = new Sun(x, y, amount);
        entities.add(sun);
    }

    @Override
    public void spawnProjectile(float x, float y, int damage,
            PlantDamageType type,
            Class<?> projectileClass) {
        if (projectileClass == PeaProjectile.class) {
            Entity pea = new PeaProjectile(x, y, damage);
            entities.add(pea);
        }
        // sau này có nhiều loại đạn thì thêm else-if ở đây
    }

    // ================== Convenience methods (giữ API cũ) ==================

    /** Cho chỗ khác cộng sun mà không cần biết tới HudController. */
    public void addSun(int amount) {
        hudController.addSun(amount);
    }

    /** Cho chỗ khác trừ sun trực tiếp từ HUD nếu cần. */
    public boolean spendSun(int cost) {
        return hudController.spendSun(cost);
    }

    /** Lấy số sun hiện tại, dùng cho card, logic khác. */
    public int getSunPoints() {
        return hudController.getSunPoints();
    }

    // ====== GAME OVER helper (optional) ======

    /**
     * Cho systems khác (vd: CollisionSystem, ZombieWaveController)
     * có thể gọi thông qua GameWorld nếu muốn.
     */
    public void setGameOver(boolean playerWon) {
        gameState.setGameOver(playerWon);
    }
}
