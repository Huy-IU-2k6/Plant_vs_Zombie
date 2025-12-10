package pvz.com.managers;

/**
 * Quản lý việc scale từ layout gốc (1920x1080) sang kích thước world hiện tại.
 *
 * Dùng cho mọi màn: main menu, resume, HUD,... để tránh lặp lại BASE_SCREEN_W/H
 * và các phép tính scale giống nhau.
 */
public final class ScaleManager {

    // Layout gốc mà bạn thiết kế UI
    public static final float BASE_SCREEN_W = DesignConfig.BASE_SCREEN_W;
    public static final float BASE_SCREEN_H = DesignConfig.BASE_SCREEN_H;

    private ScaleManager() {
        // không cho new
    }

    // ===== SCALE THEO CHIỀU CAO / RỘNG MÀN HÌNH =====

    /** scale factor dựa trên chiều cao: worldH / BASE_SCREEN_H */
    public static float getHeightScale(float worldHeight) {
        return worldHeight / BASE_SCREEN_H;
    }

    /** scale factor dựa trên chiều rộng: worldW / BASE_SCREEN_W */
    public static float getWidthScale(float worldWidth) {
        return worldWidth / BASE_SCREEN_W;
    }

    /** scale kích thước (px trên layout gốc) theo chiều cao màn hình */
    public static float scaleByHeight(float baseSize, float worldHeight) {
        return baseSize * worldHeight / BASE_SCREEN_H;
    }

    /** scale kích thước (px trên layout gốc) theo chiều rộng màn hình */
    public static float scaleByWidth(float baseSize, float worldWidth) {
        return baseSize * worldWidth / BASE_SCREEN_W;
    }

    // ===== CHUYỂN PX GỐC -> TỈ LỆ MÀN HÌNH (0..1) =====

    /** tỉ lệ trên trục X so với layout gốc (dùng cho stageW * ratio) */
    public static float ratioFromBaseWidth(float baseX) {
        return baseX / BASE_SCREEN_W;
    }

    /** tỉ lệ trên trục Y so với layout gốc (dùng cho stageH * ratio) */
    public static float ratioFromBaseHeight(float baseY) {
        return baseY / BASE_SCREEN_H;
    }

    // ===== CONVENIENCE: ĐỔI BASE-COORD -> WORLD-COORD (không xài ratio) =====

    public static float toWorldX(float baseX, float worldWidth) {
        return baseX * worldWidth / BASE_SCREEN_W;
    }

    public static float toWorldY(float baseY, float worldHeight) {
        return baseY * worldHeight / BASE_SCREEN_H;
    }

    public static float toWorldWidth(float designW, float worldWidth) {
        return designW * getWidthScale(worldWidth);
    }

    public static float toWorldHeight(float designH, float worldHeight) {
        return designH * getHeightScale(worldHeight);
    }
}
