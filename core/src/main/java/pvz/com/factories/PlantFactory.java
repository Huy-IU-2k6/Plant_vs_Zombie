package pvz.com.factories;

import pvz.com.entities.plants.Plant;
import pvz.com.entities.plants.PlantType;

import pvz.com.entities.plants.producers.SunFlower;
import pvz.com.entities.plants.shooters.Peashooter;
import pvz.com.entities.plants.defenders.Wallnut;
import pvz.com.entities.plants.bombs.CherryBomb;
import pvz.com.entities.plants.bombs.PotatoMine;

public class PlantFactory {

    // ===== CÁC FACTORY CỤ THỂ (có col, row) =====
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
        return new CherryBomb(x, y, col, row);
    }

    public static Plant createPotatoMine(float x, float y, int col, int row) {
        return new PotatoMine(x, y, col, row);
    }

    // ===== MASTER FACTORY DÙNG ENUM =====
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

    // ===== (OPTIONAL) OVERLOAD CŨ KHÔNG CÓ col/row =====
    // Nếu code cũ của bạn đang gọi createSunflower(x, y) thì:
    // hoặc là sửa hết callsite sang dùng col,row
    // hoặc tạm để mấy overload này cho đỡ lỗi compile:

    public static Plant createSunflower(float x, float y) {
        // Nếu constructor SunFlower(x, y, col, row) là dạng mới,
        // bạn có thể tạm cho col,row = -1 hoặc tính sau ở chỗ spawn.
        return new SunFlower(x, y, -1, -1);
    }

    public static Plant createPeashooter(float x, float y) {
        return new Peashooter(x, y, -1, -1);
    }

    public static Plant createWallnut(float x, float y) {
        return new Wallnut(x, y, -1, -1);
    }
}
