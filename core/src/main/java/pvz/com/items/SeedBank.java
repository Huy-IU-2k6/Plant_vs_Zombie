package pvz.com.items;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;

import pvz.com.managers.HudLayoutConfig;

public class SeedBank extends Group {

    private final Texture bgTex;
    private final Array<PlantCard> cards = new Array<>();

    private static final float TRAY_WIDTH_RATIO = HudLayoutConfig.SEEDBANK_WIDTH_RATIO;
    private static final float TRAY_MARGIN_LEFT = HudLayoutConfig.SEEDBANK_MARGIN_LEFT;
    private static final float TRAY_MARGIN_TOP = HudLayoutConfig.SEEDBANK_MARGIN_TOP;

    private static final float SAFE_PADDING_LEFT = HudLayoutConfig.SEEDBANK_SAFE_PADDING_LEFT;
    private static final float SAFE_PADDING_RIGHT = HudLayoutConfig.SEEDBANK_SAFE_PADDING_RIGHT;
    private static final float PADDING_TOP = HudLayoutConfig.SEEDBANK_PADDING_TOP;
    private static final float PADDING_BOTTOM = HudLayoutConfig.SEEDBANK_PADDING_BOTTOM;
    private static final float CARD_GAP_X = HudLayoutConfig.SEEDBANK_CARD_GAP_X;
    private static final float CARD_HEIGHT_RATIO = HudLayoutConfig.SEEDBANK_CARD_HEIGHT_RATIO;

    private static final float SUN_LABEL_CENTER_X = HudLayoutConfig.SUN_LABEL_CENTER_X;
    private static final float SUN_LABEL_CENTER_Y = HudLayoutConfig.SUN_LABEL_CENTER_Y;

    // tỉ lệ chiều rộng / chiều cao card (dựa theo kích thước gốc)
    private static final float CARD_ASPECT = PlantCard.WIDTH / PlantCard.HEIGHT;

    // ==== SUN HUD ====
    private int sunAmount = 0;
    private final Label sunLabel;

    public SeedBank(BitmapFont font) {
        bgTex = new Texture(Gdx.files.internal("images/items/seed_bank.png"));

        // style cho text sun
        Label.LabelStyle style = new Label.LabelStyle();
        style.font = font;
        style.fontColor = Color.YELLOW;

        sunLabel = new Label("0", style);
        sunLabel.setAlignment(Align.center);
        sunLabel.setFontScale(0.6f); // cho chữ nhỏ lại cho giống số 50
        addActor(sunLabel);

        // set kích thước ban đầu sau khi đã có sunLabel
        setSize(bgTex.getWidth(), bgTex.getHeight());
    }

    public void setSunAmount(int sunAmount) {
        this.sunAmount = sunAmount;
        sunLabel.setText(Integer.toString(sunAmount));
    }

    public int getSunAmount() {
        return sunAmount;
    }

    public void addCard(PlantCard card) {
        cards.add(card);
        addActor(card);
        layoutCards();
    }

    @Override
    public void setSize(float width, float height) {
        super.setSize(width, height);
        layoutCards();
        layoutSunLabel();
    }

    private void layoutSunLabel() {
        if (sunLabel == null)
            return;

        float scaleX = getWidth() / bgTex.getWidth();
        float scaleY = getHeight() / bgTex.getHeight();

        // chuyển toạ độ pixel gốc sang toạ độ sau khi scale
        float sunCenterX = SUN_LABEL_CENTER_X * scaleX;
        float sunCenterY = SUN_LABEL_CENTER_Y * scaleY;

        sunLabel.setPosition(
                sunCenterX - sunLabel.getPrefWidth() / 2f,
                sunCenterY - sunLabel.getPrefHeight() / 2f);
    }

    private void layoutCards() {
        if (cards.size == 0)
            return;

        float scaleX = getWidth() / bgTex.getWidth();
        float scaleY = getHeight() / bgTex.getHeight();

        float innerPadLeft = SAFE_PADDING_LEFT * scaleX;
        float innerPadRight = SAFE_PADDING_RIGHT * scaleX;
        float cardGapX = CARD_GAP_X * scaleX;

        float innerWidth = getWidth() - innerPadLeft - innerPadRight;
        float innerHeight = getHeight() - (PADDING_TOP + PADDING_BOTTOM) * scaleY;

        float cardHeight = innerHeight * CARD_HEIGHT_RATIO;
        float cardWidth = cardHeight * CARD_ASPECT;

        float totalCardsWidth = cards.size * cardWidth + (cards.size - 1) * cardGapX;
        if (totalCardsWidth > innerWidth) {
            cardWidth = (innerWidth - (cards.size - 1) * cardGapX) / cards.size;
            cardHeight = cardWidth / CARD_ASPECT;
        }

        float baseY = PADDING_BOTTOM * scaleY + (innerHeight - cardHeight) / 2f;

        for (int i = 0; i < cards.size; i++) {
            PlantCard card = cards.get(i);
            float x = innerPadLeft + i * (cardWidth + cardGapX);
            card.setBounds(x, baseY, cardWidth, cardHeight);
        }
    }

    public void updateLayout(float worldWidth, float worldHeight) {
        // kích thước ảnh gốc
        float textureWidth = bgTex.getWidth();
        float textureHeight = bgTex.getHeight();
        float trayAspect = textureHeight / textureWidth;

        // cho SeedBank chiếm TRAY_WIDTH_RATIO chiều ngang màn hình
        float trayWidth = worldWidth * TRAY_WIDTH_RATIO;
        float trayHeight = trayWidth * trayAspect;

        setSize(trayWidth, trayHeight);

        // canh trái + cách mép trên một đoạn (dùng constant)
        setPosition(
                TRAY_MARGIN_LEFT,
                worldHeight - trayHeight - TRAY_MARGIN_TOP);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        Color c = getColor();
        batch.setColor(c.r, c.g, c.b, c.a * parentAlpha);

        batch.draw(bgTex, getX(), getY(), getWidth(), getHeight());
        super.draw(batch, parentAlpha);
    }

    public void dispose() {
        bgTex.dispose();
    }
}
