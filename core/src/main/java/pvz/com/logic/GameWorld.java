package pvz.com.logic;

import com.badlogic.gdx.graphics.OrthographicCamera;
import java.util.List;
import pvz.com.entities.Entity;
import pvz.com.entities.Zombies.BaseZombie;
import pvz.com.entities.components.PlantDamageType;
import pvz.com.entities.plants.Plant;
import pvz.com.entities.projectiles.FrozenPeaProjectile;
import pvz.com.entities.projectiles.PeaProjectile;
import pvz.com.entities.suns.Sun;
import pvz.com.managers.GridConfig;
import pvz.com.systems.AnimationSystem;
import pvz.com.systems.ArmingSystem;
import pvz.com.systems.CleanupSystem;
import pvz.com.systems.CollisionSystem;
import pvz.com.systems.ExplosionSystem;
import pvz.com.systems.IGameSpawner;
import pvz.com.systems.ISunReceiver;
import pvz.com.systems.MovementSystem;
import pvz.com.systems.PlantAttackSystem;
import pvz.com.systems.SunPickupSystem;
import pvz.com.systems.SunProductionSystem;
import pvz.com.systems.WallnutStateSystem;

public class GameWorld implements IGameSpawner, ISunReceiver {

    private final List<Entity> entities;
    private final List<Plant> plants;
    private final GameState gameState;

    private final ZombieWaveController zombieWaveController;

    
    private final HudController hudController;

    
    private final SunProductionSystem sunSystem;
    private final WallnutStateSystem wallnutStateSystem;
    private final ExplosionSystem explosionSystem;
    private final ArmingSystem armingSystem;
    private final AnimationSystem animationSystem;
    private final PlantAttackSystem attackSystem;
    private final MovementSystem movementSystem;
    private final CollisionSystem collisionSystem;
    private final SunPickupSystem sunPickupSystem;
    private final CleanupSystem cleanupSystem;

    public GameWorld(GameState gameState,
            HudController hudController,
            List<Entity> entities,
            List<Plant> plants,
            OrthographicCamera camera,
            ZombieWaveController zombieWaveController,
            PlantGridController plantGridController) {

        this.gameState = gameState;
        this.hudController = hudController;
        this.entities = entities;
        this.plants = plants;

        this.zombieWaveController = zombieWaveController;

        
        this.sunSystem = new SunProductionSystem(this, entities);
        this.wallnutStateSystem = new WallnutStateSystem();
        this.explosionSystem = new ExplosionSystem(zombieWaveController, plantGridController);
        this.armingSystem = new ArmingSystem();
        this.animationSystem = new AnimationSystem();
        this.attackSystem = new PlantAttackSystem(this, zombieWaveController);
        this.movementSystem = new MovementSystem();
        this.collisionSystem = new CollisionSystem(entities, zombieWaveController, plantGridController);
        this.sunPickupSystem = new SunPickupSystem(entities, camera, this);
        this.cleanupSystem = new CleanupSystem(entities, plants, plantGridController);
    }

    public void update(float delta) {
        if (gameState.isGameOver())
            return;
        if (!gameState.isPlaying())
            return;

        
        sunSystem.update(delta);
        wallnutStateSystem.update(entities);
        explosionSystem.update(entities, delta);
        armingSystem.update(entities, delta);
        animationSystem.update(entities, delta);
        attackSystem.update(plants, delta);
        movementSystem.update(entities, delta);
        collisionSystem.update(delta);

        
        checkLoseCondition();
        if (!gameState.isGameOver()) {
            checkWinCondition();
        }

        if (gameState.isGameOver())
            return;

        sunPickupSystem.update(delta);
        cleanupSystem.update();
    }

    
    private void checkLoseCondition() {
        if (zombieWaveController == null)
            return;

        float loseX = GridConfig.getCellOriginX(0) - GridConfig.CELL_WIDTH * 0.35f;

        for (BaseZombie z : zombieWaveController.getZombies()) {
            if (z == null || z.isDead())
                continue;
            if (z.getX() <= loseX) {
                gameState.setGameOver(false); // playerWon = false
                return;
            }
        }
    }

    
    private void checkWinCondition() {
        if (zombieWaveController == null)
            return;

        
        if (!zombieWaveController.isWaveFinished())
            return;

        for (BaseZombie z : zombieWaveController.getZombies()) {
            if (z != null && !z.isDead()) {
                return; 
            }
        }

        gameState.setGameOver(true); 
    }

    
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

    
    @Override
    public void addSun(int amount) {
        hudController.addSun(amount);
    }

    
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
