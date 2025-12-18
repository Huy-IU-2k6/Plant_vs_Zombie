package pvz.com.managers;

/**
 * Quản lý việc scale từ layout gốc (1920x1080) sang kích thước world hiện tại.
 *
 * Dùng cho mọi màn: main menu, resume, HUD,... để tránh lặp lại BASE_SCREEN_W/H
 * và các phép tính scale giống nhau.
 */
public final class ScaleManager {

    public static final float BASE_SCREEN_W = DesignConfig.BASE_SCREEN_W;
    public static final float BASE_SCREEN_H = DesignConfig.BASE_SCREEN_H;

    private ScaleManager() {
    }

    public static float getHeightScale(float worldHeight) {
        return worldHeight / BASE_SCREEN_H;
    }

    public static float getWidthScale(float worldWidth) {
        return worldWidth / BASE_SCREEN_W;
    }

    public static float scaleByHeight(float baseSize, float worldHeight) {
        return baseSize * worldHeight / BASE_SCREEN_H;
    }

    public static float scaleByWidth(float baseSize, float worldWidth) {
        return baseSize * worldWidth / BASE_SCREEN_W;
    }

    public static float ratioFromBaseWidth(float baseX) {
        return baseX / BASE_SCREEN_W;
    }

    public static float ratioFromBaseHeight(float baseY) {
        return baseY / BASE_SCREEN_H;
    }

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
