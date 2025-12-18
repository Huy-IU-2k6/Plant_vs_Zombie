package pvz.com.logic;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.Viewport;

import pvz.com.managers.BackgroundManager;

public class WorldRenderer {

    private final BackgroundManager backgroundManager;
    private final Viewport viewport;
    private final LawnMowerController lawnMowerController;
    private final ZombieWaveController zombieWaveController;

    public WorldRenderer(BackgroundManager backgroundManager,
            Viewport viewport,
            LawnMowerController lawnMowerController,
            ZombieWaveController zombieWaveController) {
        this.backgroundManager = backgroundManager;
        this.viewport = viewport;
        this.lawnMowerController = lawnMowerController;
        this.zombieWaveController = zombieWaveController;
    }

    public void render(SpriteBatch batch, boolean isCountdown, boolean isPlaying) {
        float w = viewport.getWorldWidth();
        float h = viewport.getWorldHeight();

        if (isCountdown) {
            
            backgroundManager.renderCount(batch, w, h);
            return;
        }

        
        backgroundManager.renderMain(batch, w, h);

        
        lawnMowerController.render(batch);
        zombieWaveController.render(batch);

        
    }

    public void dispose() {
        backgroundManager.dispose();
    }
}
