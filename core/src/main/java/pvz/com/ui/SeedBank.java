package pvz.com.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.utils.Array;

public class SeedBank extends Group {

    private final Texture bgTex;
    private final Array<PlantCard> cards = new Array<>();

    private static final float SAFE_PADDING_LEFT = 310f;
    private static final float SAFE_PADDING_RIGHT = 30f;
    private static final float PADDING_TOP = 10f;
    private static final float PADDING_BOTTOM = 10f;
    private static final float CARD_GAP_X = 5f;

    private static final float CARD_HEIGHT_RATIO = 0.9f; // thẻ cao 90% vùng khả dụng
    private static final float CARD_ASPECT = PlantCard.WIDTH / PlantCard.HEIGHT;

    public SeedBank() {
        bgTex = new Texture(Gdx.files.internal("assets/images/items/seed_bank.png"));
        setSize(bgTex.getWidth(), bgTex.getHeight());
        setTransform(false);
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

        // kích thước card ban đầu
        float cardH = innerH * CARD_HEIGHT_RATIO;
        float cardW = cardH * CARD_ASPECT;

        // nếu tổng chiều rộng > vùng khả dụng thì thu nhỏ lại
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
