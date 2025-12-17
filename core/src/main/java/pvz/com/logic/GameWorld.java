package pvz.com.logic;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.List;

import pvz.com.entities.Entity;
import pvz.com.entities.Zombies.Zombies; // [NEW] Needed to check zombies for game over
import pvz.com.entities.components.PlantDamageType;
import pvz.com.entities.plants.Plant;
import pvz.com.entities.projectiles.FrozenPeaProjectile;
import pvz.com.entities.projectiles.PeaProjectile;
import pvz.com.entities.suns.Sun;
import pvz.com.managers.GridConfig; // [NEW] Needed for coordinate calculations
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

    // We need to keep this to check zombie positions in update()
    private final ZombieWaveController zombieWaveController; 

    // Systems
    private final RenderSystem renderSystem;
    private final AnimationSystem animationSystem;
    private final SunProductionSystem sunSystem;
    private final PlantAttackSystem attackSystem;
    private final MovementSystem movementSystem;
    private final CollisionSystem collisionSystem;
    private final SunPickupSystem sunPickupSystem;
    private final CleanupSystem cleanupSystem;

    // HUD is the only place holding SUN
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
        this.zombieWaveController = zombieWaveController; // Store this for later use

        this.renderSystem = new RenderSystem(batch);
        this.animationSystem = new AnimationSystem();

        this.sunSystem = new SunProductionSystem(this, entities);
        this.attackSystem = new PlantAttackSystem(this, zombieWaveController);

        this.movementSystem = new MovementSystem();

        // ===========================
        // [FIX] CollisionSystem constructor now only takes 3 arguments
        // We removed 'gameState' from here to avoid circular dependency
        // ===========================
        this.collisionSystem = new CollisionSystem(
                entities,
                zombieWaveController,
                plantGridController
                // gameState REMOVED
        );

        this.sunPickupSystem = new SunPickupSystem(entities, camera, this);
        this.cleanupSystem = new CleanupSystem(entities);
    }

    public void update(float delta) {
        // Stop updates if game is over
        if (gameState.isGameOver()) return;
        if (!gameState.isPlaying()) return;

        // 1. Update Systems
        sunSystem.update(delta);
        animationSystem.update(entities, delta);
        attackSystem.update(plants, delta);
        movementSystem.update(entities, delta);
        
        // Update collisions (Damage calculations)
        collisionSystem.update(delta);

        // 2. [NEW] Check Game Over Condition HERE (Moved from CollisionSystem)
        checkGameOverCondition();

        // Stop if game just ended
        if (gameState.isGameOver()) return;

        sunPickupSystem.update(delta);
        cleanupSystem.update();
    }

    // Logic to check if any zombie has reached the house
    private void checkGameOverCondition() {
        if (zombieWaveController == null) return;

        // Define the "Lose Line" (Left edge of column 0)
        float loseX = GridConfig.getCellOriginX(0) - GridConfig.CELL_WIDTH * 0.35f;

        for (Zombies z : zombieWaveController.getZombies()) {
            if (z.isDead()) continue;

            // If a zombie crosses the line -> GAME OVER (Player Lost)
            if (z.getX() <= loseX) {
                gameState.setGameOver(false); 
                break; 
            }
        }
    }

    public void render(SpriteBatch batch) {
        renderSystem.update(entities);
    }

    // ===== IGameSpawner =====

    @Override
    public void spawnSun(float x, float y, int amount) {
        entities.add(new Sun(x, y, amount));
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
    }

    // ===== ISunReceiver =====

    @Override
    public void addSun(int amount) {
        hudController.addSun(amount);
    }

    // ===== Helpers =====

    public SunPickupSystem getSunPickupSystem() {
        return sunPickupSystem;
    }

    public GameState getGameState() {
        return gameState;
    }

    public void addPlant(Plant plant) {
        entities.add(plant);
        plants.add(plant);
    }

    public void setGameOver(boolean playerWon) {
        gameState.setGameOver(playerWon);
    }
}