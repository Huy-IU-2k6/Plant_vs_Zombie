package pvz.com.logic;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;

import pvz.com.items.ItemType;
import pvz.com.items.PlantCard;
import pvz.com.items.SeedBank;
import pvz.com.managers.FontManager;
import pvz.com.items.CountdownActor;

public class HudController {

    private final Stage hudStage;

    private final SeedBank seedBank;
    private final Array<PlantCard> plantCards = new Array<>();
    private CountdownActor countdown;
    private final BitmapFont hudFont;

    private int sunPoints;

    public HudController(Stage hudStage, float countdownDuration, int initialSun) {
        this.hudStage = hudStage;
        this.sunPoints = initialSun;

        // Font dùng cho HUD (countdown + seed bank)
        hudFont = FontManager.getPvzFont();

        // SeedBank
        seedBank = new SeedBank(hudFont);
        seedBank.setVisible(false);
        seedBank.setSunAmount(initialSun); // sync số sun ban đầu
        hudStage.addActor(seedBank);

        // sau khi tạo stage / viewport
        float worldWidth = hudStage.getViewport().getWorldWidth();
        float worldHeight = hudStage.getViewport().getWorldHeight();

        seedBank.updateLayout(worldWidth, worldHeight);

        // Countdown
        countdown = new CountdownActor(countdownDuration, hudFont);
        countdown.setPosition(400f, 500f);
        hudStage.addActor(countdown);

        // Plant cards
        createPlantCards();
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
        float worldWidth = hudStage.getViewport().getWorldWidth();
        float worldHeight = hudStage.getViewport().getWorldHeight();

        seedBank.updateLayout(worldWidth, worldHeight);
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
    // Chỉ lưu logic sunPoints, việc vẽ giao cho SeedBank

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
        // hudFont do FontManager quản lý, không dispose ở đây
    }
}
