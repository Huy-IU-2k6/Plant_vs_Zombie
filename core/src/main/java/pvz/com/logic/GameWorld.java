package pvz.com.logic;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.List;

import pvz.com.entities.Entity;
import pvz.com.entities.components.PlantDamageType;
import pvz.com.entities.plants.Plant;
import pvz.com.entities.projectiles.FrozenPeaProjectile; // [FIX] Added
import pvz.com.entities.projectiles.PeaProjectile;
import pvz.com.entities.suns.Sun;
import pvz.com.systems.AnimationSystem; // [FIX] Added
import pvz.com.systems.CleanupSystem;
import pvz.com.systems.CollisionSystem;
import pvz.com.systems.IGameSpawner;
import pvz.com.systems.ISunReceiver; // [FIX] Added
import pvz.com.systems.MovementSystem;
import pvz.com.systems.PlantAttackSystem;
import pvz.com.systems.RenderSystem;
import pvz.com.systems.SunPickupSystem;
import pvz.com.systems.SunProductionSystem;

// [FIX] Implements ISunReceiver so SunPickupSystem accepts 'this'
public class GameWorld implements IGameSpawner, ISunReceiver {

    private final List<Entity> entities;
    private final List<Plant> plants;
    private final GameState gameState;

    // ECS systems
    private final RenderSystem renderSystem;
    private final AnimationSystem animationSystem; // [FIX] Added AnimationSystem
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
        
        // [FIX] Initialize AnimationSystem
        this.animationSystem = new AnimationSystem(); 
        
        this.sunSystem = new SunProductionSystem(this, entities);
        
        // [FIX] Pass zombieWaveController to PlantAttackSystem
        this.attackSystem = new PlantAttackSystem(this, zombieWaveController); 
        
        this.movementSystem = new MovementSystem();
        this.collisionSystem = new CollisionSystem(
                entities,
                zombieWaveController,
                plantGridController);
                
        // [FIX] SunPickupSystem now accepts ISunReceiver (this class implements it)
        this.sunPickupSystem = new SunPickupSystem(entities, camera, this);
        
        this.cleanupSystem = new CleanupSystem(entities);
    }

    public void update(float delta) {
        if (!gameState.isPlaying()) {
            return;
        }

        sunSystem.update(delta);
        
        // [FIX] Update Animations
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

    // ====== IGameSpawner & ISunReceiver Implementation ======

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
        // [FIX] Handle FrozenPeaProjectile
        else if (projectileClass == FrozenPeaProjectile.class) {
            Entity frozenPea = new FrozenPeaProjectile(x, y, damage);
            entities.add(frozenPea);
        }
    }

    @Override
    public void addSun(int amount) {
        hudController.addSun(amount);
    }

    // ================== Getters & Helpers ==================

    public SunPickupSystem getSunPickupSystem() {
        return sunPickupSystem;
    }

    public void addPlant(Plant plant) {
        entities.add(plant);
        plants.add(plant);
    }

    public boolean spendSun(int cost) {
        return hudController.spendSun(cost);
    }

    public int getSunPoints() {
        return hudController.getSunPoints();
    }

    public void setGameOver(boolean playerWon) {
        gameState.setGameOver(playerWon);
    }
}