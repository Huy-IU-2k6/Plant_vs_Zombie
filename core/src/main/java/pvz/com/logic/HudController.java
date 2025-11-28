package pvz.com.logic;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;

import pvz.com.items.ItemType;
import pvz.com.items.PlantCard;
import pvz.com.items.SeedBank;
import pvz.com.managers.FontManager;
import pvz.com.ui.CountdownActor;

public class HudController {

    // ===== SeedBank layout =====
    private static final float SEED_BANK_HEIGHT = 110f;
    private static final float SEED_BANK_MARGIN_TOP = 20f;
    private static final float SEED_BANK_MARGIN_LEFT = 50f;

    private final Stage hudStage;

    private final SeedBank seedBank;
    private final Array<PlantCard> plantCards = new Array<>();
    private CountdownActor countdown;
    private final BitmapFont hudFont;

    private int sunPoints;

    public HudController(Stage hudStage, float countdownDuration, int initialSun) {
        this.hudStage = hudStage;
        this.sunPoints = initialSun;

        // SeedBank
        seedBank = new SeedBank();
        layoutSeedBank();
        seedBank.setVisible(false);
        hudStage.addActor(seedBank);

        // Font
        hudFont = FontManager.getPvzFont();

        // Countdown
        countdown = new CountdownActor(countdownDuration, hudFont);
        countdown.setPosition(400f, 500f);
        hudStage.addActor(countdown);

        // Plant cards
        createPlantCards();
    }

    private void layoutSeedBank() {
        float hudH = hudStage.getViewport().getWorldHeight();

        float originalW = seedBank.getWidth();
        float originalH = seedBank.getHeight();

        float scale = SEED_BANK_HEIGHT / originalH;
        float trayW = originalW * scale;
        float trayH = originalH * scale;

        seedBank.setSize(trayW, trayH);
        seedBank.setPosition(
                SEED_BANK_MARGIN_LEFT,
                hudH - trayH - SEED_BANK_MARGIN_TOP);
    }

    private void createPlantCards() {
        for (ItemType type : ItemType.values()) {
            PlantCard card = new PlantCard(type);
            plantCards.add(card);
            seedBank.addCard(card);
        }
    }

    public void resize(int width, int height) {
        hudStage.getViewport().update(width, height, true);
        layoutSeedBank();
    }

    // ===== Countdown =====

    public boolean isCountdownFinished() {
        return countdown != null && countdown.isFinished();
    }

    public void onCountdownFinished() {
        if (countdown != null) {
            countdown.remove();
            countdown = null;
        }
        unlockPlantCards();
        seedBank.setVisible(true);
    }

    private void unlockPlantCards() {
        for (PlantCard card : plantCards) {
            card.setLockedByGame(false);
        }
    }

    // ===== Sun HUD =====

    public void drawSunHud(SpriteBatch batch) {
        float sbX = seedBank.getX();
        float sbY = seedBank.getY();

        float textX = sbX + 55f;
        float textY = sbY + 42f;

        hudFont.draw(batch, String.valueOf(sunPoints), textX, textY);
    }

    public void addSun(int amount) {
        sunPoints += amount;
    }

    public boolean spendSun(int cost) {
        if (sunPoints < cost)
            return false;
        sunPoints -= cost;
        return true;
    }

    public int getSunPoints() {
        return sunPoints;
    }

    public SeedBank getSeedBank() {
        return seedBank;
    }

    public Array<PlantCard> getPlantCards() {
        return plantCards;
    }

    public void dispose() {
        seedBank.dispose();
    }
}
