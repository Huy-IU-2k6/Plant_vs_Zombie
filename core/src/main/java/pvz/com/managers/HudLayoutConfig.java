package pvz.com.managers;

/**
 * Cấu hình layout cho HUD (SeedBank, Countdown, v.v.).
 * Toàn bộ "số magic" liên quan đến vị trí / tỉ lệ HUD nên gom vào đây.
 */
public class HudLayoutConfig {

    // ===== SEEDBANK: VỊ TRÍ & KÍCH THƯỚC TRÊN HUD =====

    /** SeedBank chiếm bao nhiêu phần trăm chiều ngang HUD (worldWidth) */
    public static final float SEEDBANK_WIDTH_RATIO = 0.4f;

    /** Khoảng cách từ mép trái màn hình HUD tới SeedBank (world units) */
    public static final float SEEDBANK_MARGIN_LEFT = 50f;

    /** Khoảng cách từ mép trên màn hình HUD xuống SeedBank (world units) */
    public static final float SEEDBANK_MARGIN_TOP = 20f;

    // ===== SEEDBANK: PADDING BÊN TRONG KHAY (TÍNH THEO TEXTURE GỐC) =====

    public static final float SEEDBANK_SAFE_PADDING_LEFT = 310f;
    public static final float SEEDBANK_SAFE_PADDING_RIGHT = 30f;
    public static final float SEEDBANK_PADDING_TOP = 10f;
    public static final float SEEDBANK_PADDING_BOTTOM = 10f;

    /**
     * Khoảng cách giữa các PlantCard bên trong SeedBank (theo texture gốc, sẽ scale
     * theo)
     */
    public static final float SEEDBANK_CARD_GAP_X = 5f;

    /** Card cao bằng bao nhiêu phần trăm chiều cao vùng trong SeedBank */
    public static final float SEEDBANK_CARD_HEIGHT_RATIO = 0.9f;

    // ===== SUN LABEL TRÊN SEEDBANK =====
    // Toạ độ tâm số "50" trên texture gốc của seed_bank
    public static final float SUN_LABEL_CENTER_X = 180f;
    public static final float SUN_LABEL_CENTER_Y = 25f;

    public static final float BASE_SUN_FONT_SCALE = 0.6f;

    // ===== COUNTDOWN =====

    // Toạ độ design gốc (1920x1080) của countdown
    public static final float COUNTDOWN_POS_X_DESIGN = 400f;
    public static final float COUNTDOWN_POS_Y_DESIGN = 500f;

    public static float getCountdownPosXRatio() {
        return ScaleManager.ratioFromBaseWidth(COUNTDOWN_POS_X_DESIGN);
    }

    public static float getCountdownPosYRatio() {
        return ScaleManager.ratioFromBaseHeight(COUNTDOWN_POS_Y_DESIGN);
    }

    /** Toạ độ X thực tế của countdown trên world hiện tại */
    public static float getCountdownWorldX(float worldWidth) {
        // Dùng ScaleManager để chuyển từ toạ độ design -> world
        return ScaleManager.toWorldX(COUNTDOWN_POS_X_DESIGN, worldWidth);
        // hoặc worldWidth * getCountdownPosXRatio();
    }

    /** Toạ độ Y thực tế của countdown trên world hiện tại */
    public static float getCountdownWorldY(float worldHeight) {
        return ScaleManager.toWorldY(COUNTDOWN_POS_Y_DESIGN, worldHeight);
        // hoặc worldHeight * getCountdownPosYRatio();
    }

    // ===== HELPER SCALE FONT (giữ API cũ nhưng delegate sang ScaleManager) =====

    /** scale theo X so với design */
    public static float getScaleX(float worldWidth) {
        return ScaleManager.getWidthScale(worldWidth);
    }

    /** scale theo Y so với design */
    public static float getScaleY(float worldHeight) {
        return ScaleManager.getHeightScale(worldHeight);
    }
}
