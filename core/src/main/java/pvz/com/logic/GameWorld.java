package pvz.com.logic;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.List;

import pvz.com.entities.Entity;
import pvz.com.entities.components.PlantDamageType;
import pvz.com.entities.plants.Plant;
import pvz.com.entities.projectiles.FrozenPeaProjectile;
import pvz.com.entities.projectiles.PeaProjectile;
import pvz.com.entities.suns.Sun;
import pvz.com.systems.AnimationSystem;
import pvz.com.systems.CleanupSystem;
import pvz.com.systems.CollisionSystem;
import pvz.com.systems.IGameSpawner;
import pvz.com.systems.ISunReceiver;
import pvz.com.systems.MovementSystem;
import pvz.com.systems.PlantAttackSystem;
import pvz.com.systems.RenderSystem;
import pvz.com.systems.SunPickupSystem;
import pvz.com.systems.SunProductionSystem;

public class GameWorld implements IGameSpawner, ISunReceiver {

    private final List<Entity> entities;
    private final List<Plant> plants;
    private final GameState gameState;

    // Systems
    private final RenderSystem renderSystem;
    private final AnimationSystem animationSystem;
    private final SunProductionSystem sunSystem;
    private final PlantAttackSystem attackSystem;
    private final MovementSystem movementSystem;
    private final CollisionSystem collisionSystem;
    private final SunPickupSystem sunPickupSystem;
    private final CleanupSystem cleanupSystem;

    // HUD là nơi giữ SUN duy nhất
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

        this.renderSystem = new RenderSystem(batch);
        this.animationSystem = new AnimationSystem();

        this.sunSystem = new SunProductionSystem(this, entities);
        this.attackSystem = new PlantAttackSystem(this, zombieWaveController);

        this.movementSystem = new MovementSystem();
        this.collisionSystem = new CollisionSystem(
                entities,
                zombieWaveController,
                plantGridController);

        // SunPickupSystem sẽ gọi addSun(...) qua ISunReceiver
        this.sunPickupSystem = new SunPickupSystem(entities, camera, this);

        this.cleanupSystem = new CleanupSystem(entities);
    }

    public void update(float delta) {
        if (!gameState.isPlaying())
            return;

        sunSystem.update(delta);
        animationSystem.update(entities, delta);

        attackSystem.update(plants, delta);
        movementSystem.update(entities, delta);
        collisionSystem.update(delta);
        sunPickupSystem.update(delta);
        cleanupSystem.update();
    }

    public void render(SpriteBatch batch) {
        renderSystem.update(entities);
    }

    // ===== IGameSpawner =====

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
            entities.add(new PeaProjectile(x, y, damage));
        } else if (projectileClass == FrozenPeaProjectile.class) {
            entities.add(new FrozenPeaProjectile(x, y, damage));
        }
        // Nếu còn loại khác thì thêm ở đây
    }

    // ===== ISunReceiver =====

    @Override
    public void addSun(int amount) {
        // Sun chỉ update ở HUD (owner duy nhất)
        hudController.addSun(amount);
    }

    // ===== Helpers =====

    public SunPickupSystem getSunPickupSystem() {
        return sunPickupSystem;
    }

    public void addPlant(Plant plant) {
        entities.add(plant);
        plants.add(plant);
    }

    public void setGameOver(boolean playerWon) {
        gameState.setGameOver(playerWon);
    }
}
