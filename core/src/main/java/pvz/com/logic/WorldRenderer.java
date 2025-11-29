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
            // Màn hình đếm ngược
            backgroundManager.renderCount(batch, w, h);
            return;
        }

        // Màn chính
        backgroundManager.renderMain(batch, w, h);

        // Lawn mower + zombie
        lawnMowerController.render(batch);
        zombieWaveController.render(batch);

        // KHÔNG vẽ sun HUD nữa, phần đó do HudController/hudStage xử lý
    }

    public void dispose() {
        backgroundManager.dispose();
    }
}
