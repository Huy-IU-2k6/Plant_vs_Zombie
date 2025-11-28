package pvz.com.factories;

import pvz.com.entities.plants.Plant;
import pvz.com.entities.plants.defenders.Wallnut;
import pvz.com.entities.plants.producers.SunFlower;
import pvz.com.entities.plants.shooters.Peashooter;

public class PlantFactory {

    // Static Factory Method
    public static Plant createSunflower(float x, float y) {
        return new SunFlower(x, y);
    }

    public static Plant createPeashooter(float x, float y) {
        return new Peashooter(x, y);
    }

    public static Plant createWallnut(float x, float y) {
        return new Wallnut(x, y);
    }

    /*
     * Khi thêm cây mới:
     * 1. Tạo class X extends Plant có constructor (float x, float y)
     * 2. Thêm hàm createX(x, y) ở đây.
     */
}
