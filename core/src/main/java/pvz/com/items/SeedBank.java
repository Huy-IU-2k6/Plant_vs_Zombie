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
import pvz.com.managers.ScaleManager;

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

    private static final float BASE_SUN_FONT_SCALE = HudLayoutConfig.BASE_SUN_FONT_SCALE;

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
        
        // [QUAN TRỌNG] Khởi tạo scale an toàn để tránh crash ngay khi tạo
        sunLabel.setFontScale(1f); 
        
        addActor(sunLabel);

        // set kích thước ban đầu sau khi đã có sunLabel
        setSize(bgTex.getWidth(), bgTex.getHeight());
        
        // [QUAN TRỌNG] Đảm bảo group không bị scale 0
        this.setScale(1f); 
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
        // [AN TOÀN] Tránh width/height = 0 gây lỗi chia cho 0
        if (width <= 1f) width = 1f;
        if (height <= 1f) height = 1f;

        super.setSize(width, height);
        layoutCards();
        layoutSunLabel();
    }

    private void layoutSunLabel() {
        if (sunLabel == null)
            return;

        float texW = (bgTex.getWidth() > 0) ? bgTex.getWidth() : 1f;
        float texH = (bgTex.getHeight() > 0) ? bgTex.getHeight() : 1f;

        float scaleX = getWidth() / texW;
        float scaleY = getHeight() / texH;

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

        float texW = (bgTex.getWidth() > 0) ? bgTex.getWidth() : 1f;
        float texH = (bgTex.getHeight() > 0) ? bgTex.getHeight() : 1f;

        float scaleX = getWidth() / texW;
        float scaleY = getHeight() / texH;

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
        // [AN TOÀN] Nếu màn hình chưa init xong (size=0), bỏ qua layout
        if (worldWidth <= 0 || worldHeight <= 0) return;

        // ===== KÍCH THƯỚC SEEDBANK =====
        float trayWidth = worldWidth * TRAY_WIDTH_RATIO;

        // Giữ tỉ lệ gốc của texture
        float textureAspect = (float) bgTex.getHeight() / (float) bgTex.getWidth();
        float trayHeight = trayWidth * textureAspect;

        setSize(trayWidth, trayHeight);

        // ===== VỊ TRÍ SEEDBANK =====
        float x = ScaleManager.toWorldX(TRAY_MARGIN_LEFT, worldWidth);
        float marginTopWorld = ScaleManager.toWorldY(TRAY_MARGIN_TOP, worldHeight);

        float y = worldHeight - trayHeight - marginTopWorld;

        setPosition(x, y);

        // scale theo chiều cao màn hình
        float fontScale = ScaleManager.getHeightScale(worldHeight);

        // [FIX CRASH] Đảm bảo scale không bao giờ bằng 0
        // LibGDX BitmapFont sẽ crash nếu scaleX hoặc scaleY = 0
        float finalScale = BASE_SUN_FONT_SCALE * fontScale;
        if (finalScale < 0.01f) finalScale = 0.01f; 

        sunLabel.setFontScale(finalScale);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        // [FIX CRASH] Nếu SeedBank đang bị scale về 0 (ví dụ animation ẩn),
        // đừng vẽ con của nó (Label), vì Label sẽ tính toán font scale = 0 -> Crash.
        if (getScaleX() < 0.01f || getScaleY() < 0.01f) {
            return;
        }

        Color c = getColor();
        batch.setColor(c.r, c.g, c.b, c.a * parentAlpha);

        batch.draw(bgTex, getX(), getY(), getWidth(), getHeight());
        
        // Chỉ gọi super.draw (vẽ Label con) khi scale an toàn
        super.draw(batch, parentAlpha);
    }

    public void dispose() {
        bgTex.dispose();
    }
}