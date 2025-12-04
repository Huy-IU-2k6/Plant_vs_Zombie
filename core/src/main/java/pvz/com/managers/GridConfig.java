package pvz.com.managers;

/**
 * Cấu hình grid (lawn) dùng chung cho Plants & Zombies.
 *
 * Hệ toạ độ libGDX: (0,0) ở góc trái dưới màn hình.
 */
public class GridConfig {

    // Số hàng / cột của lawn
    public static final int ROWS = 5;
    public static final int COLS = 9;

    // Kích thước 1 ô (theo world units)
    public static final float CELL_WIDTH = 62f;
    public static final float CELL_HEIGHT = 100f;

    // Góc trái dưới của ô (row=0, col=0) trên world
    public static final float START_X = 255f;
    public static final float START_Y = 60f;

    // =========================================================
    // CENTER OF CELL
    // =========================================================

    /** Tâm ô theo col (theo worldX) */
    public static float getCellCenterX(int col) {
        return START_X + col * CELL_WIDTH + CELL_WIDTH / 2f;
    }

    /** Tâm ô theo row (theo worldY) */
    public static float getCellCenterY(int row) {
        return START_Y + row * CELL_HEIGHT + CELL_HEIGHT / 2f;
    }

    // =========================================================
    // ORIGIN (GÓC TRÁI DƯỚI CỦA Ô)
    // =========================================================

    /** Góc trái dưới của ô theo col (worldX) */
    public static float getCellOriginX(int col) {
        return START_X + col * CELL_WIDTH;
    }

    /** Góc trái dưới của ô theo row (worldY) */
    public static float getCellOriginY(int row) {
        return START_Y + row * CELL_HEIGHT;
    }

    // =========================================================
    // WORLD → GRID
    // =========================================================

    /** Lấy col từ tọa độ worldX */
    public static int worldToCol(float worldX) {
        float localX = worldX - START_X; // toạ độ tương đối trong lawn
        return (int) (localX / CELL_WIDTH); // floor
    }

    /** Lấy row từ tọa độ worldY */
    public static int worldToRow(float worldY) {
        float localY = worldY - START_Y;
        return (int) (localY / CELL_HEIGHT);
    }

    /** Kiểm tra row/col có nằm trong grid không */
    public static boolean isInsideGrid(int row, int col) {
        return row >= 0 && row < ROWS && col >= 0 && col < COLS;
    }

    public static int[] worldToNearestCell(float worldX, float worldY) {
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
        return getCellCenterX(col) - actorWidth / 2f;
    }

    /**
     * Tính worldY để vẽ 1 Actor có height đã biết sao cho nó nằm giữa ô row.
     * Trả về góc trái dưới (origin) của Actor.
     */
    public static float getActorYForCell(int row, float actorHeight) {
        return getCellCenterY(row) - actorHeight / 2f;
    }
}
