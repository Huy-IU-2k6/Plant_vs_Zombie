package pvz.com.factories;

import pvz.com.entities.plants.bombs.CherryBomb;
import pvz.com.entities.plants.Plant;
import pvz.com.entities.plants.defenders.Wallnut;
import pvz.com.entities.plants.producers.SunFlower;
import pvz.com.entities.plants.shooters.Peashooter;
import pvz.com.entities.plants.bombs.PotatoMine;

// Bạn có thể cần enum này để phương thức tổng quát hoạt động (xem bên dưới)
import pvz.com.entities.plants.PlantType; 

public class PlantFactory {

    // --- CÁCH 1: Factory Method cụ thể (Đã cập nhật thêm col, row) ---
    
    public static Plant createSunflower(float x, float y, int col, int row) {
        // Truyền đủ 4 tham số như constructor chúng ta đã sửa ở bước trước
        return new SunFlower(x, y, col, row);
    }

    public static Plant createPeashooter(float x, float y, int col, int row) {
        return new Peashooter(x, y, col, row);
    }

    public static Plant createWallnut(float x, float y, int col, int row) {
        return new Wallnut(x, y, col, row);
    }

    // --- CÁCH 2: Nâng cấp "Master Factory" (Khuyên dùng) ---
    
    /* Lý do cần hàm này: 
       Khi người chơi click vào thẻ bài trên UI, thẻ bài đó thường chứa một Enum (ví dụ PlantType.PEASHOOTER).
       Thay vì viết switch-case dài ngoằng trong GameScreen, ta gọi hàm này.
    */
    public static Plant createPlant(PlantType type, float x, float y, int col, int row) {
        switch (type) {
            case SUNFLOWER:
                return new SunFlower(x, y, col, row);
            case PEASHOOTER:
                return new Peashooter(x, y, col, row);
            case WALLNUT:
                return new Wallnut(x, y, col, row);
            case CHERRY_BOMB:
                return new CherryBomb(x, y, col, row);
            case POTATO_MINE:
                return new PotatoMine(x, y, col, row);
            default:
                return null; // Hoặc throw exception
        }
    }
}
