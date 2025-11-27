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

        this.hudFont = FontManager.getPvzFont();

        // --- SeedBank ---
        this.seedBank = new SeedBank();
        layoutSeedBank();
        seedBank.setVisible(false);
        hudStage.addActor(seedBank);

        // --- Countdown ---
        this.countdown = new CountdownActor(countdownDuration, hudFont);
        countdown.setPosition(400f, 500f);
        hudStage.addActor(countdown);

        // --- Plant cards ---
        createPlantCards();
    }

    // ============== SeedBank & plant cards ==============

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

    private void unlockPlantCards() {
        for (PlantCard card : plantCards) {
            card.setLockedByGame(false);
        }
    }

    // ============== Countdown flow ==============

    public boolean isCountdownFinished() {
        return countdown != null && countdown.isFinished();
    }

    /** Gọi khi countdown xong: ẩn countdown, mở SeedBank, unlock cards. */
    public void onCountdownFinished() {
        if (countdown != null) {
            countdown.remove();
            countdown = null;
        }
        seedBank.setVisible(true);
        unlockPlantCards();
    }

    // ============== Sun HUD ==============

    public void addSun(int amount) {
        sunPoints += amount;
    }

    public boolean spendSun(int cost) {
        if (sunPoints < cost)
            return false;
        sunPoints -= cost;
        return true;
    }

    public void drawSunHud(SpriteBatch batch) {
        float sbX = seedBank.getX();
        float sbY = seedBank.getY();

        float textX = sbX + 55f;
        float textY = sbY + 42f;

        hudFont.draw(batch, String.valueOf(sunPoints), textX, textY);
    }

    /** Logic khi click card – GameScreen sẽ gọi delegate tới. */
    public void onPlantCardClicked(PlantCard card) {
        if (!card.canUse(sunPoints))
            return;
        if (!spendSun(card.type.cost))
            return;

        card.triggerUse();
        // TODO: đặt mode kéo/đặt plant, việc này xử lý ở GameScreen
    }

    // ============== Stage helper ==============

    public Stage getStage() {
        return hudStage;
    }

    public void resize(int width, int height) {
        hudStage.getViewport().update(width, height, true);
        layoutSeedBank();
    }

    public void actAndDraw(float delta) {
        hudStage.act(delta);
        hudStage.draw();
    }

    public void dispose() {
        seedBank.dispose();
        // font do FontManager quản lý, không dispose ở đây
    }
}
