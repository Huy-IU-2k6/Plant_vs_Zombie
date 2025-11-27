package pvz.com.managers;

/**
 * Cấu hình grid (lawn) dùng chung cho Plants & Zombies.
 * Tất cả logic quy đổi row/col <-> worldX/worldY nên dồn hết vào đây.
 */
public class GridConfig {

    // Số hàng / cột của lawn
    public static final int ROWS = 5;
    public static final int COLS = 9;

    // Kích thước 1 ô (theo world units, tùy theo ông scale stage thế nào)
    public static final float CELL_WIDTH = 80f;
    public static final float CELL_HEIGHT = 100f;

    // Góc trái dưới của ô (row=0, col=0) trên world
    // 2 giá trị này ông chỉnh lại cho khớp với background hiện tại.
    public static final float START_X = 200f;
    public static final float START_Y = 60f;

    /** Tâm ô theo col */
    public static float getCellCenterX(int col) {
        return START_X + col * CELL_WIDTH + CELL_WIDTH / 2f;
    }

    /** Tâm ô theo row */
    public static float getCellCenterY(int row) {
        return START_Y + row * CELL_HEIGHT + CELL_HEIGHT / 2f;
    }

    /** Lấy col từ tọa độ worldX */
    public static int worldToCol(float worldX) {
        return (int) ((worldX - START_X) / CELL_WIDTH);
    }

    /** Lấy row từ tọa độ worldY */
    public static int worldToRow(float worldY) {
        return (int) ((worldY - START_Y) / CELL_HEIGHT);
    }

    /** Kiểm tra row/col có nằm trong grid không */
    public static boolean isInsideGrid(int row, int col) {
        return row >= 0 && row < ROWS && col >= 0 && col < COLS;
    }

    /**
     * Trả về X để đặt Actor sao cho đứng giữa ô (theo col) với width đã biết.
     */
    public static float getActorXForCell(int col, float actorWidth) {
        return getCellCenterX(col) - actorWidth / 2f;
    }

    /**
     * Trả về Y để đặt Actor sao cho đứng giữa ô (theo row) với height đã biết.
     */
    public static float getActorYForCell(int row, float actorHeight) {
        return getCellCenterY(row) - actorHeight / 2f;
    }
}
