package pvz.com.factories;

import pvz.com.entities.plants.Plant;
import pvz.com.entities.plants.producers.SunFlower;

public class PlantFactory {

    // Static Factory Method
    public static Plant createSunflower(float x, float y) {
        // Việc khởi tạo được giấu kín trong này
        return new SunFlower(x, y);
    }

    /* Nguyên lý OCP (Open/Closed):
    Khi bạn muốn thêm cây mới (VD: Peashooter), bạn chỉ cần:
    1. Tạo file PeaShooter.java
    2. Thêm hàm createPeashooter() vào đây.
    -> Không cần sửa code cũ của createSunflower() hay code của GameScreen.
    */
}