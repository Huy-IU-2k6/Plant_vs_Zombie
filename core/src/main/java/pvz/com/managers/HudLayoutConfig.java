package pvz.com.managers;

/**
 * Cấu hình layout cho HUD (SeedBank, Countdown, v.v.).
 * Toàn bộ "số magic" liên quan đến vị trí / tỉ lệ HUD nên gom vào đây.
 */
public class HudLayoutConfig {

    public static final float SEEDBANK_WIDTH_RATIO = 0.4f;

    public static final float SEEDBANK_MARGIN_LEFT = 50f;

    public static final float SEEDBANK_MARGIN_TOP = 20f;

    public static final float SEEDBANK_SAFE_PADDING_LEFT = 310f;
    public static final float SEEDBANK_SAFE_PADDING_RIGHT = 30f;
    public static final float SEEDBANK_PADDING_TOP = 10f;
    public static final float SEEDBANK_PADDING_BOTTOM = 10f;

    public static final float SEEDBANK_CARD_GAP_X = 5f;

    public static final float SEEDBANK_CARD_HEIGHT_RATIO = 0.9f;

    public static final float SUN_LABEL_CENTER_X = 180f;
    public static final float SUN_LABEL_CENTER_Y = 25f;

    public static final float BASE_SUN_FONT_SCALE = 0.6f;

    public static final float COUNTDOWN_POS_X_DESIGN = 400f;
    public static final float COUNTDOWN_POS_Y_DESIGN = 500f;

    public static float getCountdownPosXRatio() {
        return ScaleManager.ratioFromBaseWidth(COUNTDOWN_POS_X_DESIGN);
    }

    public static float getCountdownPosYRatio() {
        return ScaleManager.ratioFromBaseHeight(COUNTDOWN_POS_Y_DESIGN);
    }

    public static float getCountdownWorldX(float worldWidth) {
        return ScaleManager.toWorldX(COUNTDOWN_POS_X_DESIGN, worldWidth);
    }

    public static float getCountdownWorldY(float worldHeight) {
        return ScaleManager.toWorldY(COUNTDOWN_POS_Y_DESIGN, worldHeight);
    }

    public static float getScaleX(float worldWidth) {
        return ScaleManager.getWidthScale(worldWidth);
    }

    public static float getScaleY(float worldHeight) {
        return ScaleManager.getHeightScale(worldHeight);
    }
}
