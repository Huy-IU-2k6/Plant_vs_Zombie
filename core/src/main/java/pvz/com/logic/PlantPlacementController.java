package pvz.com.logic;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;

import pvz.com.entities.plants.Plant;
import pvz.com.entities.plants.PlantType;
import pvz.com.factories.PlantFactory;
import pvz.com.items.PlantCard;
import pvz.com.managers.GridConfig;

public class PlantPlacementController {

    private final Viewport viewport;
    private final HudController hudController;
    private final PlantGridController plantGridController;
    private final GameWorld gameWorld;

    public PlantPlacementController(Viewport viewport,
            HudController hudController,
            PlantGridController plantGridController,
            GameWorld gameWorld) {
        this.viewport = viewport;
        this.hudController = hudController;
        this.plantGridController = plantGridController;
        this.gameWorld = gameWorld;
    }

    /**
     * Mode click-to-place (nếu bạn vẫn dùng):
     * click card -> trừ sun + trigger cooldown.
     *
     * Lưu ý: click-to-place kiểu “chọn card rồi click ô để đặt” thường cần thêm
     * state "selectedCard".
     * Code này giữ đúng hành vi cũ của bạn: click là trừ sun + vào cooldown ngay.
     */
    public void handleCardClicked(PlantCard card, boolean isPlaying) {
        if (!isPlaying)
            return;

        int currentSun = hudController.getSunPoints();
        if (!card.canUse(currentSun))
            return;

        int cost = card.getDef().cost();
        if (!hudController.spendSun(cost))
            return;

        card.triggerUse();
    }

    /**
     * Kéo-thả card -> đặt plant lên grid (snap nearest cell).
     */
    public void handleCardDragged(PlantCard card,
            float screenX, float screenY,
            boolean isPlaying) {
        if (!isPlaying)
            return;

        // screen -> world
        Vector2 world = viewport.unproject(new Vector2(screenX, screenY));

        // Snap nearest cell
        int[] cell = GridConfig.worldToNearestCell(world.x, world.y);
        int row = cell[0];
        int col = cell[1];

        if (row < 0 || col < 0 || !GridConfig.isInsideGrid(row, col)) {
            // thả ngoài lawn
            return;
        }

        int currentSun = hudController.getSunPoints();
        if (!card.canUse(currentSun))
            return;

        int cost = card.getDef().cost();

        // Trừ sun trước để tránh “free plant”
        if (!hudController.spendSun(cost))
            return;

        boolean spawned = spawnPlantFromCardAtGrid(card, row, col);
        if (!spawned) {
            // Ô bận / factory lỗi -> hoàn sun
            hudController.addSun(cost);
            return;
        }

        // OK -> cooldown
        card.triggerUse();
    }

    private boolean spawnPlantFromCardAtGrid(PlantCard card, int row, int col) {
        if (plantGridController.isCellOccupied(row, col)) {
            return false;
        }

        PlantType plantType = card.type; // PlantType luôn, khỏi map
        Plant plant = PlantFactory.createPlantAtCell(plantType, col, row);
        if (plant == null)
            return false;

        gameWorld.addPlant(plant);
        plantGridController.registerPlantAtCell(plant, row, col);
        return true;
    }
}
