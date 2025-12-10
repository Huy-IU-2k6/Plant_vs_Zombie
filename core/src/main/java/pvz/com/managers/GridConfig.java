package pvz.com.managers;

public class GridConfig {

    // Số hàng / cột của lawn (không cần scale)
    public static final int ROWS = DesignConfig.ROWS;
    public static final int COLS = DesignConfig.COLS;

    // Kích thước 1 ô & vị trí start trên WORLD (sau khi đã scale)
    public static float CELL_WIDTH;
    public static float CELL_HEIGHT;
    public static float START_X;
    public static float START_Y;

    private static boolean initialized = false;

    /**
     * Gọi 1 lần sau khi biết worldWidth / worldHeight (vd: sau khi tạo viewport).
     *
     * @param worldWidth  kích thước world theo trục X (vd: camera viewportWidth)
     * @param worldHeight kích thước world theo trục Y (vd: camera viewportHeight)
     */
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

    // =========================================================
    // CENTER OF CELL
    // =========================================================

    /** Tâm ô theo col (theo worldX) */
    public static float getCellCenterX(int col) {
        checkInit();
        return START_X + col * CELL_WIDTH + CELL_WIDTH / 2f;
    }

    /** Tâm ô theo row (theo worldY) */
    public static float getCellCenterY(int row) {
        checkInit();
        return START_Y + row * CELL_HEIGHT + CELL_HEIGHT / 2f;
    }

    // =========================================================
    // ORIGIN (GÓC TRÁI DƯỚI CỦA Ô)
    // =========================================================

    /** Góc trái dưới của ô theo col (worldX) */
    public static float getCellOriginX(int col) {
        checkInit();
        return START_X + col * CELL_WIDTH;
    }

    /** Góc trái dưới của ô theo row (worldY) */
    public static float getCellOriginY(int row) {
        checkInit();
        return START_Y + row * CELL_HEIGHT;
    }

    // =========================================================
    // WORLD → GRID
    // =========================================================

    /** Lấy col từ tọa độ worldX */
    public static int worldToCol(float worldX) {
        checkInit();
        float localX = worldX - START_X; // toạ độ tương đối trong lawn
        return (int) (localX / CELL_WIDTH); // floor
    }

    /** Lấy row từ tọa độ worldY */
    public static int worldToRow(float worldY) {
        checkInit();
        float localY = worldY - START_Y;
        return (int) (localY / CELL_HEIGHT);
    }

    /** Kiểm tra row/col có nằm trong grid không */
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

        // Optional: nếu muốn chặt chẽ hơn, kiểm tra xem có quá xa lawn không
        float maxRadius = Math.max(CELL_WIDTH, CELL_HEIGHT) * 0.8f;
        float cx = getCellCenterX(bestCol);
        float cy = getCellCenterY(bestRow);
        float dx = worldX - cx;
        float dy = worldY - cy;
        if (dx * dx + dy * dy > maxRadius * maxRadius) {
            // thả quá xa grid → coi như ngoài sân
            return new int[] { -1, -1 };
        }

        return new int[] { bestRow, bestCol };
    }

    // =========================================================
    // HỖ TRỢ CANH GIỮA ACTOR TRONG Ô
    // =========================================================

    /**
     * Tính worldX để vẽ 1 Actor có width đã biết sao cho nó nằm giữa ô col.
     * Trả về góc trái dưới (origin) của Actor.
     */
    public static float getActorXForCell(int col, float actorWidth) {
        checkInit();
        return getCellCenterX(col) - actorWidth / 2f;
    }

    /**
     * Tính worldY để vẽ 1 Actor có height đã biết sao cho nó nằm giữa ô row.
     * Trả về góc trái dưới (origin) của Actor.
     */
    public static float getActorYForCell(int row, float actorHeight) {
        checkInit();
        return getCellCenterY(row) - actorHeight / 2f;
    }
}
