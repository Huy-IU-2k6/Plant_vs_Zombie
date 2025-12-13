package pvz.com.logic;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;

import pvz.com.entities.plants.PlantType;
import pvz.com.items.PlantCard;
import pvz.com.items.PlantCatalog;
import pvz.com.items.SeedBank;
import pvz.com.managers.FontManager;
import pvz.com.items.CountdownActor;
import pvz.com.managers.HudLayoutConfig;

public class HudController {

    private static final float COUNTDOWN_POS_X_RATIO = HudLayoutConfig.getCountdownPosXRatio();
    private static final float COUNTDOWN_POS_Y_RATIO = HudLayoutConfig.getCountdownPosYRatio();

    // ===== FIELDS =====
    private final Stage hudStage;

    private final SeedBank seedBank;
    private final Array<PlantCard> plantCards = new Array<>();
    private CountdownActor countdown;
    private final BitmapFont hudFont;

    private int sunPoints;

    public HudController(Stage hudStage, float countdownDuration, int initialSun) {
        this.hudStage = hudStage;
        this.sunPoints = initialSun;

        hudFont = FontManager.getPvzFont();

        // SeedBank
        seedBank = new SeedBank(hudFont);
        seedBank.setVisible(false);
        seedBank.setSunAmount(initialSun);
        hudStage.addActor(seedBank);

        // Countdown
        countdown = new CountdownActor(countdownDuration);
        hudStage.addActor(countdown);

        // Plant cards
        createPlantCards();

        // Layout ban đầu
        updateHudLayout();
    }

    private void createPlantCards() {
        // Chỉ tạo card cho những plant đã đăng ký trong PlantCatalog
        for (PlantType type : PlantCatalog.types()) {
            PlantCard card = new PlantCard(type);
            plantCards.add(card);
            seedBank.addCard(card);
        }
    }

    private void updateHudLayout() {
        float worldWidth = hudStage.getViewport().getWorldWidth();
        float worldHeight = hudStage.getViewport().getWorldHeight();

        seedBank.updateLayout(worldWidth, worldHeight);

        if (countdown != null) {
            float countdownX = worldWidth * COUNTDOWN_POS_X_RATIO;
            float countdownY = worldHeight * COUNTDOWN_POS_Y_RATIO;
            countdown.setPosition(countdownX, countdownY);
        }
    }

    public void resize(int width, int height) {
        hudStage.getViewport().update(width, height, true);
        updateHudLayout();
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
    public void addSun(int amount) {
        sunPoints += amount;
        seedBank.setSunAmount(sunPoints);
    }

    public boolean spendSun(int cost) {
        if (sunPoints < cost)
            return false;

        sunPoints -= cost;
        seedBank.setSunAmount(sunPoints);
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
