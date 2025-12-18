package pvz.com.managers;

public class DesignConfig {
    public static final float BASE_SCREEN_W = 1920f;
    public static final float BASE_SCREEN_H = 1080f;

    public static final float START_X = 588f;
    public static final float START_Y = 54f;

    public static final int ROWS = 5;
    public static final int COLS = 9;

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

    public static final float SHOVEL_PAD_X = 815f;
    public static final float SHOVEL_PAD_Y = 40f;
    public static final float SHOVEL_ICON_SIZE = 72f;

    public static final float SHOVEL_REFUND_RATIO = 1.0f;

    public static final String SHOVEL_ICON_PATH = "images/items/Shovel_Box.png";
    public static final String SHOVEL_GHOST_PATH = "images/items/Shovel.png";

    private DesignConfig() {
    }

    public static final float SUN_COOL_DOWN = 10f;

    public static final float DAMAGE_PER_SECOND = 10f;

    public static final float PLANT_FRAME_DURATION = 0.06f;
}
