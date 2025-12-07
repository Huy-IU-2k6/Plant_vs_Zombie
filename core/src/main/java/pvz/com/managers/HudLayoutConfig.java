package pvz.com.managers;

/**
 * Cấu hình layout cho HUD (SeedBank, Countdown, v.v.).
 * Toàn bộ "số magic" liên quan đến vị trí / tỉ lệ HUD nên gom vào đây.
 */
public class HudLayoutConfig {

    // ===== BASE DESIGN RESOLUTION =====
    // Dùng làm chuẩn để scale font, vị trí tương đối, v.v.
    public static final float BASE_SCREEN_W = 1920f;
    public static final float BASE_SCREEN_H = 1080f;

    // ===== SEEDBANK: VỊ TRÍ & KÍCH THƯỚC TRÊN HUD =====

    /** SeedBank chiếm bao nhiêu phần trăm chiều ngang HUD (worldWidth) */
    public static final float SEEDBANK_WIDTH_RATIO = 0.4f;

    /** Khoảng cách từ mép trái màn hình HUD tới SeedBank */
    public static final float SEEDBANK_MARGIN_LEFT = 50f;

    /** Khoảng cách từ mép trên màn hình HUD xuống SeedBank */
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

    // ===== COUNTDOWN =====

    // Toạ độ design gốc (1920x1080) của countdown
    public static final float COUNTDOWN_POS_X_DESIGN = 400f;
    public static final float COUNTDOWN_POS_Y_DESIGN = 500f;

    /** Tỉ lệ X của countdown so với chiều ngang màn hình design */
    public static float getCountdownPosXRatio() {
        return COUNTDOWN_POS_X_DESIGN / BASE_SCREEN_W;
    }

    /** Tỉ lệ Y của countdown so với chiều dọc màn hình design */
    public static float getCountdownPosYRatio() {
        return COUNTDOWN_POS_Y_DESIGN / BASE_SCREEN_H;
    }

    // ===== HELPER SCALE FONT (OPTIONAL) =====

    /** scale theo X so với design */
    public static float getScaleX(float worldWidth) {
        return worldWidth / BASE_SCREEN_W;
    }

    /** scale theo Y so với design */
    public static float getScaleY(float worldHeight) {
        return worldHeight / BASE_SCREEN_H;
    }
}
