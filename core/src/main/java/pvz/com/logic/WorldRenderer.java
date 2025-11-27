package pvz.com.logic;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.Viewport;

import pvz.com.Zombies.NormalZombie;
import pvz.com.items.LawnMower;
import pvz.com.items.SeedBank;
import pvz.com.managers.BackgroundManager;

public class WorldRenderer {

    private final Viewport viewport;
    private final BackgroundManager backgroundManager;
    private final Array<LawnMower> lawnMowers;
    private final Array<NormalZombie> zombies;
    private final SeedBank seedBank;
    private final BitmapFont hudFont;

    public WorldRenderer(Viewport viewport,
            BackgroundManager backgroundManager,
            Array<LawnMower> lawnMowers,
            Array<NormalZombie> zombies,
            SeedBank seedBank,
            BitmapFont hudFont) {
        this.viewport = viewport;
        this.backgroundManager = backgroundManager;
        this.lawnMowers = lawnMowers;
        this.zombies = zombies;
        this.seedBank = seedBank;
        this.hudFont = hudFont;
    }

    public void render(SpriteBatch batch,
            boolean isCountdown,
            boolean showSunHud,
            int sunPoints) {
        float w = viewport.getWorldWidth();
        float h = viewport.getWorldHeight();

        // Background
        if (isCountdown) {
            backgroundManager.renderCount(batch, w, h);
        } else {
            backgroundManager.renderMain(batch, w, h);
        }

        // Lawn mowers
        for (LawnMower mower : lawnMowers) {
            mower.render(batch);
        }

        // Zombies
        for (NormalZombie z : zombies) {
            z.draw(batch, 1f);
        }

        // Sun HUD
        if (showSunHud) {
            drawSunHud(batch, sunPoints);
        }
    }

    private void drawSunHud(SpriteBatch batch, int sunPoints) {
        float sbX = seedBank.getX();
        float sbY = seedBank.getY();

        float textX = sbX + 55f;
        float textY = sbY + 42f;

        hudFont.draw(batch, String.valueOf(sunPoints), textX, textY);
    }
}
