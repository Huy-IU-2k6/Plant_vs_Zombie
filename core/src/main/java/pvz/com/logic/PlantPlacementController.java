package pvz.com.logic;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;

import pvz.com.entities.Entity;
import pvz.com.entities.components.GridCellComponent;
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
     * Mode click-to-place (giữ hành vi cũ của bạn):
     * click card -> trừ sun + trigger cooldown ngay.
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

        // screen -> world (theo viewport)
        Vector2 world = viewport.unproject(new Vector2(screenX, screenY));

        // snap nearest cell (nhờ PlantGridController để gom logic)
        int[] cell = plantGridController.worldToNearestCell(world.x, world.y);
        int row = cell[0];
        int col = cell[1];

        // ngoài lawn
        if (row < 0 || col < 0 || !GridConfig.isInsideGrid(row, col))
            return;

        // check sun + cooldown
        int currentSun = hudController.getSunPoints();
        if (!card.canUse(currentSun))
            return;

        int cost = card.getDef().cost();

        // Trừ sun trước để tránh “free plant”
        if (!hudController.spendSun(cost))
            return;

        boolean placed = tryPlacePlantFromCard(card, row, col);
        if (!placed) {
            // Ô bận / spawn lỗi -> hoàn sun
            hudController.addSun(cost);
            return;
        }

        // OK -> cooldown
        card.triggerUse();
    }

    /**
     * Gọi khi entity (plant) bị remove/die/shovel để gỡ khỏi grid đúng ô.
     * Ưu tiên GridCellComponent; nếu không có thì fallback theo world pos.
     */
    public void unregisterPlantForEntity(Entity entity, Vector2 pos) {
        GridCellComponent cell = entity.getComponent(GridCellComponent.class);
        if (cell != null) {
            plantGridController.unregisterPlantAtCell(cell.row, cell.col);
        } else {
            int[] c = GridConfig.worldToNearestCell(pos.x, pos.y);
            plantGridController.unregisterPlantAtCell(c[0], c[1]);
        }
    }

    /**
     * 1 nơi duy nhất làm “đặt cây”:
     * - check occupied
     * - factory create
     * - add vào gameWorld
     * - register vào grid
     */
    private boolean tryPlacePlantFromCard(PlantCard card, int row, int col) {
        if (plantGridController.isCellOccupied(row, col))
            return false;

        PlantType type = card.type; // PlantType luôn
        Plant plant = PlantFactory.createPlantAtCell(type, col, row);
        if (plant == null)
            return false;

        gameWorld.addPlant(plant);
        plantGridController.registerPlantAtCell(plant, row, col);
        return true;
    }
}
