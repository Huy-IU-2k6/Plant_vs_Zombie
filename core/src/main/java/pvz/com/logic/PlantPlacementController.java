package pvz.com.logic;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;

import pvz.com.entities.plants.Plant;
import pvz.com.entities.plants.PlantType;
import pvz.com.factories.PlantFactory;
import pvz.com.items.ItemType;
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
     * Xử lý mode click-to-place: click card → trừ sun, bật trạng thái dùng card.
     * (Logic y như GameScreen.onPlantCardClicked cũ nhưng có check isPlaying)
     */
    public void handleCardClicked(PlantCard card, boolean isPlaying) {
        if (!isPlaying) {
            return;
        }

        if (!hudController.spendSun(card.type.cost)) {
            return;
        }

        card.triggerUse();
    }

    /**
     * Xử lý kéo-thả card → đặt plant lên grid.
     */
    public void handleCardDragged(PlantCard card,
            float screenX, float screenY,
            boolean isPlaying) {
        if (!isPlaying) {
            return;
        }

        // screen -> world
        Vector2 world = viewport.unproject(new Vector2(screenX, screenY));

        // Snap vào ô gần nhất
        int[] cell = GridConfig.worldToNearestCell(world.x, world.y);
        int row = cell[0];
        int col = cell[1];

        if (row < 0 || col < 0) {
            // thả ngoài lawn
            return;
        }

        int currentSun = hudController.getSunPoints();
        if (!card.canUse(currentSun)) {
            // không đủ sun hoặc đang cooldown
            return;
        }

        // Trừ sun trước (tránh case plant xuất hiện nhưng không trừ sun)
        if (!hudController.spendSun(card.type.cost)) {
            return;
        }

        boolean spawned = spawnPlantFromCardAtGrid(card, row, col);
        if (!spawned) {
            // Ô bận / factory lỗi → trả lại sun
            hudController.addSun(card.type.cost);
            return;
        }

        card.triggerUse();
    }

    private boolean spawnPlantFromCardAtGrid(PlantCard card, int row, int col) {
        if (plantGridController.isCellOccupied(row, col)) {
            // ô đã có cây, không trồng chồng
            return false;
        }

        PlantType plantType = toPlantType(card.type);
        Plant plant = PlantFactory.createPlantAtCell(plantType, col, row);
        if (plant == null) {
            return false;
        }

        gameWorld.addPlant(plant);
        plantGridController.registerPlantAtCell(plant, row, col);
        return true;
    }

    private PlantType toPlantType(ItemType itemType) {
        switch (itemType) {
            case SUNFLOWER:
                return PlantType.SUNFLOWER;
            case PEASHOOTER:
                return PlantType.PEASHOOTER;
            case WALLNUT:
                return PlantType.WALLNUT;
            case CHERRYBOMB:
                return PlantType.CHERRY_BOMB;
            case POTATOMINE:
                return PlantType.POTATO_MINE;

            // mấy thằng chưa map riêng
            case CHOMPER:
            case REPEATER:
            case SNOWPEA:
                return PlantType.SNOW_PEA;
            default:
                return PlantType.PEASHOOTER;
        }
    }
}
