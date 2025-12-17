package pvz.com.managers;

public class DesignConfig {
    public static final float BASE_SCREEN_W = 1920f;
    public static final float BASE_SCREEN_H = 1080f;

    public static final float START_X = 588f;
    public static final float START_Y = 54f;

    // Số hàng / cột của lawn
    public static final int ROWS = 5;
    public static final int COLS = 9;

    // Kích thước 1 ô (theo world units)
    public static final float CELL_WIDTH = 147.6f;
    public static final float CELL_HEIGHT = 180f;

    public static final float SCALE_FACTOR = 0.8f;
    public static final float FIXED_WIDTH = CELL_WIDTH * SCALE_FACTOR;
    public static final float FIXED_HEIGHT = CELL_HEIGHT * SCALE_FACTOR;

    public static final float ZOMBIE_W = 160f;
    public static final float ZOMBIE_H = 180f;

    private static final float PEA_SIZE_RATIO = 0.21f;

    public static final float PEA_WIDTH = FIXED_HEIGHT * PEA_SIZE_RATIO;
    public static final float PEA_HEIGHT = FIXED_HEIGHT * PEA_SIZE_RATIO;

    private static final float SUN_SIZE_RATIO = 0.42f;

    public static final float SUN_WIDTH = FIXED_HEIGHT * SUN_SIZE_RATIO;
    public static final float SUN_HEIGHT = FIXED_HEIGHT * SUN_SIZE_RATIO;

    public static final float DAMAGE_PER_SECOND = 10f;
}
