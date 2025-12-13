package pvz.com.logic;

import pvz.com.entities.plants.PlantType;

public interface IPlantPlacer {

    /**
     * Kiểm tra chỗ (row, col) có đặt được cây này không
     * (đủ sun, ô trống, game state cho phép, v.v.)
     */
    boolean canPlacePlant(PlantType type, int row, int col);

    /**
     * Thực sự spawn cây vào ECS / world ở (row, col).
     * Trả về true nếu đặt thành công.
     */
    boolean placePlant(PlantType type, int row, int col);
}
