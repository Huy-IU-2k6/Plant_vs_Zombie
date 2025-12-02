package pvz.com.logic;

import pvz.com.items.ItemType;

public interface IPlantPlacer {
    /**
     * Kiểm tra chỗ (gridX, gridY) có đặt được cây này không (đủ sun, ô trống, v.v.)
     */
    boolean canPlacePlant(ItemType type, int gridX, int gridY);

    /** Thực sự spawn cây vào ECS / world ở gridX, gridY */
    void placePlant(ItemType type, int gridX, int gridY);
}
