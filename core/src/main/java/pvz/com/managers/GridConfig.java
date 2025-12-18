package pvz.com.managers;

public class GridConfig {

    public static final int ROWS = DesignConfig.ROWS;
    public static final int COLS = DesignConfig.COLS;

    public static float CELL_WIDTH;
    public static float CELL_HEIGHT;
    public static float START_X;
    public static float START_Y;

    private static boolean initialized = false;

    public static void init(float worldWidth, float worldHeight) {
        // scale kích thước ô theo thiết kế gốc 1920x1080
        CELL_WIDTH = ScaleManager.scaleByWidth(DesignConfig.CELL_WIDTH, worldWidth);
        CELL_HEIGHT = ScaleManager.scaleByHeight(DesignConfig.CELL_HEIGHT, worldHeight);

        // toạ độ góc trái dưới lawn
        START_X = ScaleManager.toWorldX(DesignConfig.START_X, worldWidth);
        START_Y = ScaleManager.toWorldY(DesignConfig.START_Y, worldHeight);

        initialized = true;
    }

    private static void checkInit() {
        if (!initialized) {
            // fallback: nếu quên init thì dùng giá trị base (1920x1080)
            CELL_WIDTH = DesignConfig.CELL_WIDTH;
            CELL_HEIGHT = DesignConfig.CELL_HEIGHT;
            START_X = DesignConfig.START_X;
            START_Y = DesignConfig.START_Y;
            initialized = true;
        }
    }

    public static float getCellCenterX(int col) {
        checkInit();
        return START_X + col * CELL_WIDTH + CELL_WIDTH / 2f;
    }

    public static float getCellCenterY(int row) {
        checkInit();
        return START_Y + row * CELL_HEIGHT + CELL_HEIGHT / 2f;
    }

    public static float getCellOriginX(int col) {
        checkInit();
        return START_X + col * CELL_WIDTH;
    }

    public static float getCellOriginY(int row) {
        checkInit();
        return START_Y + row * CELL_HEIGHT;
    }

    public static int worldToCol(float worldX) {
        checkInit();
        float localX = worldX - START_X;
        return (int) (localX / CELL_WIDTH);
    }

    public static int worldToRow(float worldY) {
        checkInit();
        float localY = worldY - START_Y;
        return (int) (localY / CELL_HEIGHT);
    }

    public static boolean isInsideGrid(int row, int col) {
        return row >= 0 && row < ROWS && col >= 0 && col < COLS;
    }

    public static int[] worldToNearestCell(float worldX, float worldY) {
        checkInit();

        int bestRow = -1;
        int bestCol = -1;
        float bestDist2 = Float.MAX_VALUE;

        for (int row = 0; row < ROWS; row++) {
            float cy = getCellCenterY(row);
            for (int col = 0; col < COLS; col++) {
                float cx = getCellCenterX(col);
                float dx = worldX - cx;
                float dy = worldY - cy;
                float dist2 = dx * dx + dy * dy;
                if (dist2 < bestDist2) {
                    bestDist2 = dist2;
                    bestRow = row;
                    bestCol = col;
                }
            }
        }

        float maxRadius = Math.max(CELL_WIDTH, CELL_HEIGHT) * 0.8f;
        float cx = getCellCenterX(bestCol);
        float cy = getCellCenterY(bestRow);
        float dx = worldX - cx;
        float dy = worldY - cy;
        if (dx * dx + dy * dy > maxRadius * maxRadius) {
            return new int[] { -1, -1 };
        }

        return new int[] { bestRow, bestCol };
    }

    public static float getActorXForCell(int col, float actorWidth) {
        checkInit();
        return getCellCenterX(col) - actorWidth / 2f;
    }

    public static float getActorYForCell(int row, float actorHeight) {
        checkInit();
        return getCellCenterY(row) - actorHeight / 2f;
    }
}
