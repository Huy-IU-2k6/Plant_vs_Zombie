package pvz.com.factories;

import pvz.com.entities.plants.Plant;
import pvz.com.entities.plants.PlantType;

import pvz.com.entities.plants.producers.SunFlower;
import pvz.com.entities.plants.shooters.Peashooter;
import pvz.com.entities.plants.defenders.Wallnut;
import pvz.com.entities.plants.bombs.CherryBomb;
import pvz.com.entities.plants.bombs.PotatoMine;

import pvz.com.managers.GridConfig;

public class PlantFactory {

    // =========================================================
    // API CHÍNH: TẠO PLANT THEO Ô GRID (col, row)
    // =========================================================

    /**
     * Tạo Plant đặt vào đúng ô grid (col, row).
     * Factory sẽ tự tính worldX/worldY dựa vào GridConfig.
     */
    public static Plant createPlantAtCell(PlantType type, int col, int row) {
        float x = GridConfig.getCellOriginX(col);
        float y = GridConfig.getCellOriginY(row);
        return createPlant(type, x, y, col, row);
    }

    // =========================================================
    // MASTER FACTORY DÙNG ENUM + TOẠ ĐỘ WORLD
    // =========================================================

    /**
     * Tạo Plant với toạ độ world (x, y) + vị trí grid (col, row).
     * Dùng khi bạn đã tính x, y ở chỗ khác (ví dụ canh giữa custom).
     */
    public static Plant createPlant(PlantType type, float x, float y, int col, int row) {
        switch (type) {
            case SUNFLOWER:
                return createSunflower(x, y, col, row);
            case PEASHOOTER:
                return createPeashooter(x, y, col, row);
            case WALLNUT:
                return createWallnut(x, y, col, row);
            case CHERRY_BOMB:
                return createCherryBomb(x, y, col, row);
            case POTATO_MINE:
                return createPotatoMine(x, y, col, row);
            default:
                throw new IllegalArgumentException("Unknown plant type: " + type);
        }
    }

    // =========================================================
    // CÁC FACTORY CỤ THỂ (GIỮ PUBLIC CHO DỄ DÙNG)
    // =========================================================

    public static Plant createSunflower(float x, float y, int col, int row) {
        return new SunFlower(x, y, col, row);
    }

    public static Plant createPeashooter(float x, float y, int col, int row) {
        return new Peashooter(x, y, col, row);
    }

    public static Plant createWallnut(float x, float y, int col, int row) {
        return new Wallnut(x, y, col, row);
    }

    public static Plant createCherryBomb(float x, float y, int col, int row) {
        float w = 90f;
        float h = 90f;

        float cx = GridConfig.getActorXForCell(col, w);
        float cy = GridConfig.getActorYForCell(row, h);

        float CHERRY_BOMB_OFFSET_Y = 55f;
        float CHERRY_BOMB_OFFSET_X = 15f;

        return new CherryBomb(cx - CHERRY_BOMB_OFFSET_X, cy - CHERRY_BOMB_OFFSET_Y, col, row);
    }

    public static Plant createPotatoMine(float x, float y, int col, int row) {
        return new PotatoMine(x, y, col, row);
    }

    // =========================================================
    // (OPTIONAL) NẾU MUỐN BỎ HẲN OVERLOAD CŨ
    // =========================================================
    // Đã bỏ mấy hàm createSunflower(x, y) / createPeashooter(x, y) dùng col,row =
    // -1
    // để tránh bug khó hiểu. Nếu code cũ còn gọi, sửa sang dùng:
    //
    // PlantFactory.createPlantAtCell(PlantType.SUNFLOWER, col, row)
    //
    // hoặc
    //
    // PlantFactory.createPlant(PlantType.SUNFLOWER, x, y, col, row)
}
