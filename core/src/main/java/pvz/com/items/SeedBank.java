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

public class SeedBank extends Group {

    private final Texture bgTex;
    private final Array<PlantCard> cards = new Array<>();

    private static final float SAFE_PADDING_LEFT = 310f;
    private static final float SAFE_PADDING_RIGHT = 30f;
    private static final float PADDING_TOP = 10f;
    private static final float PADDING_BOTTOM = 10f;
    private static final float CARD_GAP_X = 5f;

    private static final float CARD_HEIGHT_RATIO = 0.9f;
    private static final float CARD_ASPECT = PlantCard.WIDTH / PlantCard.HEIGHT;

    // ==== SUN HUD ====
    private int sunAmount = 0;
    private final Label sunLabel;

    // toạ độ tâm của số 50 trên texture gốc (pixel trên ảnh gốc)
    // nếu lệch chút thì chỉnh 2 số này
    private static final float SUN_LABEL_CENTER_X = 175f;
    private static final float SUN_LABEL_CENTER_Y = 25f;

    public SeedBank(BitmapFont font) {
        bgTex = new Texture(Gdx.files.internal("images/items/seed_bank.png"));
        setTransform(false);

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

        float padLeft = SAFE_PADDING_LEFT * scaleX;
        float padRight = SAFE_PADDING_RIGHT * scaleX;
        float gapX = CARD_GAP_X * scaleX;

        float innerW = getWidth() - padLeft - padRight;
        float innerH = getHeight() - (PADDING_TOP + PADDING_BOTTOM) * scaleY;

        float cardH = innerH * CARD_HEIGHT_RATIO;
        float cardW = cardH * CARD_ASPECT;

        float totalW = cards.size * cardW + (cards.size - 1) * gapX;
        if (totalW > innerW) {
            cardW = (innerW - (cards.size - 1) * gapX) / cards.size;
            cardH = cardW / CARD_ASPECT;
        }

        float baseY = PADDING_BOTTOM * scaleY + (innerH - cardH) / 2f;

        for (int i = 0; i < cards.size; i++) {
            PlantCard card = cards.get(i);
            float x = padLeft + i * (cardW + gapX);
            card.setBounds(x, baseY, cardW, cardH);
        }
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
