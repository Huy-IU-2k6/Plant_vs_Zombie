package pvz.com.factories;

import pvz.com.entities.plants.Plant;
import pvz.com.entities.plants.PlantType;

import pvz.com.entities.plants.producers.SunFlower;
import pvz.com.entities.plants.shooters.Peashooter;
import pvz.com.entities.plants.shooters.SnowPea;
import pvz.com.entities.plants.defenders.Wallnut;
import pvz.com.entities.plants.bombs.CherryBomb;
import pvz.com.entities.plants.bombs.PotatoMine;
import pvz.com.entities.plants.shooters.Repeater;

import pvz.com.managers.GridConfig;

public class PlantFactory {

    // =========================================================
    // SCALE CHO CÁC PLANT (tương đối theo CELL)
    // =========================================================

    private static final float SUNFLOWER_SCALE_X = 0.7f;
    private static final float SUNFLOWER_SCALE_Y = 0.8f;

    private static final float PEASHOOTER_SCALE_X = 0.7f;
    private static final float PEASHOOTER_SCALE_Y = 0.8f;

    private static final float WALLNUT_SCALE_X = 0.9f;
    private static final float WALLNUT_SCALE_Y = 0.9f;

    private static final float POTATOMINE_SCALE_X = 0.7f;
    private static final float POTATOMINE_SCALE_Y = 0.6f;

    // CherryBomb dùng size riêng (gif vuông)
    private static final float CHERRY_BOMB_WIDTH = 90f;
    private static final float CHERRY_BOMB_HEIGHT = 90f;

    private static final float SNOWPEA_SCALE_X = 0.7f;
    private static final float SNOWPEA_SCALE_Y = 0.8f;
    // offset để CherryBomb nằm đẹp hơn trong ô
    private static final float CHERRY_BOMB_OFFSET_X = 15f;
    private static final float CHERRY_BOMB_OFFSET_Y = 55f;

    private static final float REPEATER_SCALE_X = 0.55f;
    private static final float REPEATER_SCALE_Y = 0.55f;
    // ========================================================= API CHÍNH: TẠO
    // PLANT THEO Ô GRID (col, row)
    // =========================================================

    /**
     * Tạo Plant đặt vào đúng ô grid (col, row).
     * Tự canh giữa ô dựa theo kích thước mong muốn.
     */
    public static Plant createPlantAtCell(PlantType type, int col, int row) {
        switch (type) {
            case SUNFLOWER:
                return createSunflowerAtCell(col, row);
            case PEASHOOTER:
                return createPeashooterAtCell(col, row);
            case WALLNUT:
                return createWallnutAtCell(col, row);
            case CHERRYBOMB:
                return createCherryBombAtCell(col, row);
            case POTATOMINE:
                return createPotatoMineAtCell(col, row);
            case SNOWPEA: // [FIX] Thêm case SnowPea
                return createSnowPeaAtCell(col, row);
            case REPEATER:
                return createRepeaterAtCell(col, row);
            default:
                throw new IllegalArgumentException("Unknown plant type: " + type);
        }
    }

    // =========================================================
    // IMPLEMENT CHO TỪNG LOẠI PLANT
    // =========================================================

    public static Plant createSunflowerAtCell(int col, int row) {
        float w = GridConfig.CELL_WIDTH * SUNFLOWER_SCALE_X;
        float h = GridConfig.CELL_HEIGHT * SUNFLOWER_SCALE_Y;

        float x = GridConfig.getActorXForCell(col, w);
        float y = GridConfig.getActorYForCell(row, h);

        return new SunFlower(x, y, col, row);
    }

    public static Plant createPeashooterAtCell(int col, int row) {
        float w = GridConfig.CELL_WIDTH * PEASHOOTER_SCALE_X;
        float h = GridConfig.CELL_HEIGHT * PEASHOOTER_SCALE_Y;

        float x = GridConfig.getActorXForCell(col, w);
        float y = GridConfig.getActorYForCell(row, h);

        return new Peashooter(x, y, col, row);
    }

    public static Plant createWallnutAtCell(int col, int row) {
        float w = GridConfig.CELL_WIDTH * WALLNUT_SCALE_X;
        float h = GridConfig.CELL_HEIGHT * WALLNUT_SCALE_Y;

        float x = GridConfig.getActorXForCell(col, w);
        float y = GridConfig.getActorYForCell(row, h);

        return new Wallnut(x, y, col, row);
    }

    public static Plant createCherryBombAtCell(int col, int row) {
        float cx = GridConfig.getCellCenterX(col);
        float cy = GridConfig.getCellCenterY(row);

        // canh giữa rồi trừ offset nhỏ cho giống game gốc
        float x = cx - CHERRY_BOMB_WIDTH / 2f - CHERRY_BOMB_OFFSET_X;
        float y = cy - CHERRY_BOMB_HEIGHT / 2f - CHERRY_BOMB_OFFSET_Y;

        return new CherryBomb(x, y, col, row);
    }

    public static Plant createPotatoMineAtCell(int col, int row) {
        float w = GridConfig.CELL_WIDTH * POTATOMINE_SCALE_X;
        float h = GridConfig.CELL_HEIGHT * POTATOMINE_SCALE_Y;

        float x = GridConfig.getActorXForCell(col, w);
        float y = GridConfig.getActorYForCell(row, h);

        return new PotatoMine(x, y, col, row);
    }

    public static Plant createSnowPeaAtCell(int col, int row) {
        float w = GridConfig.CELL_WIDTH * SNOWPEA_SCALE_X;
        float h = GridConfig.CELL_HEIGHT * SNOWPEA_SCALE_Y;
        float x = GridConfig.getActorXForCell(col, w);
        float y = GridConfig.getActorYForCell(row, h);
        return new SnowPea(x, y, col, row);
    }

    public static Plant createRepeaterAtCell(int col, int row) {
        float w = GridConfig.CELL_WIDTH * REPEATER_SCALE_X;
        float h = GridConfig.CELL_HEIGHT * REPEATER_SCALE_Y;
        float x = GridConfig.getActorXForCell(col, w);
        float y = GridConfig.getActorYForCell(row, h);
        return new Repeater(x, y, col, row);
    }

    // =========================================================
    // MASTER FACTORY DÙNG ENUM + TOẠ ĐỘ WORLD (NẾU CẦN)
    // =========================================================

    /**
     * Nếu ở chỗ khác cậu đã có sẵn x, y (muốn custom vị trí) thì dùng hàm này.
     * Nhưng đa số trường hợp nên dùng createPlantAtCell cho đỡ lệch grid.
     */
    public static Plant createPlant(PlantType type, float x, float y, int col, int row) {
        switch (type) {
            case SUNFLOWER:
                return new SunFlower(x, y, col, row);
            case PEASHOOTER:
                return new Peashooter(x, y, col, row);
            case WALLNUT:
                return new Wallnut(x, y, col, row);
            case CHERRYBOMB:
                return new CherryBomb(x, y, col, row);
            case POTATOMINE:
                return new PotatoMine(x, y, col, row);
            case SNOWPEA: // [FIX] Thêm case SnowPea vào đây luôn
                return new SnowPea(x, y, col, row);
            case REPEATER:
                return new Repeater(x, y, col, row);
            default:
                throw new IllegalArgumentException("Unknown plant type: " + type);
        }
    }
}
