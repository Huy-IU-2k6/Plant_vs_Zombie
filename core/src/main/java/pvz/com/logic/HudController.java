package pvz.com.logic;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.utils.Array;

import pvz.com.entities.plants.PlantType;
import pvz.com.items.PlantCard;
import pvz.com.items.PlantCatalog;
import pvz.com.items.SeedBank;
import pvz.com.items.CountdownActor;
import pvz.com.items.Shovel;
import pvz.com.managers.FontManager;
import pvz.com.managers.HudLayoutConfig;
import pvz.com.systems.ISunReceiver;

public class HudController implements ISunReceiver {

    private static final float COUNTDOWN_POS_X_RATIO = HudLayoutConfig.getCountdownPosXRatio();
    private static final float COUNTDOWN_POS_Y_RATIO = HudLayoutConfig.getCountdownPosYRatio();

    // ===== FIELDS =====
    private final Stage hudStage;

    private final SeedBank seedBank;
    private final Array<PlantCard> plantCards = new Array<>();
    private CountdownActor countdown;
    private final BitmapFont hudFont;

    // shovel
    private final Shovel shovel;

    // SUN owner duy nhất nằm ở đây
    private int sunPoints;

    public HudController(Stage hudStage,
            float countdownDuration,
            int initialSun,
            PlantGridController plantGridController,
            ShovelController shovelController) {

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

        // Shovel: ✅ ẨN cho tới khi countdown xong
        shovel = new Shovel(plantGridController, shovelController, this);
        shovel.setVisible(false);
        shovel.setTouchable(Touchable.disabled);
        hudStage.addActor(shovel);

        // Layout ban đầu
        updateHudLayout();
    }

    private void createPlantCards() {
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

        // đặt shovel góc trái trên (dù đang ẩn vẫn set pos ok)
        shovel.layoutTopLeft(worldWidth, worldHeight);
    }

    /** GameScreen gọi khi resize */
    public void resize(int width, int height) {
        hudStage.getViewport().update(width, height, true);
        updateHudLayout();
    }

    // ===== Countdown =====

    public boolean isCountdownFinished() {
        return countdown != null && countdown.isFinished();
    }

    public void onCountdownFinished() {
        // remove countdown
        if (countdown != null) {
            countdown.remove();
            countdown = null;
        }

        // mở seedbank + unlock cards
        unlockPlantCards();
        seedBank.setVisible(true);

        // ✅ hiện shovel sau countdown
        shovel.setVisible(true);
        shovel.setTouchable(Touchable.enabled);
    }

    private void unlockPlantCards() {
        for (PlantCard card : plantCards) {
            card.setLockedByGame(false);
        }
    }

    // ===== Sun HUD (owner duy nhất) =====

    @Override
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

    // ===== Getters =====

    public Stage getHudStage() {
        return hudStage;
    }

    public SeedBank getSeedBank() {
        return seedBank;
    }

    public Array<PlantCard> getPlantCards() {
        return plantCards;
    }

    public Shovel getShovel() {
        return shovel;
    }

    public void dispose() {
        seedBank.dispose();
        if (shovel != null)
            shovel.dispose();
    }
}
