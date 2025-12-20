package pvz.com.entities.factories;

import pvz.com.entities.plants.Plant;
import pvz.com.entities.plants.PlantType;

import pvz.com.entities.plants.producers.SunFlower;
import pvz.com.entities.plants.shooters.Peashooter;
import pvz.com.entities.plants.shooters.SnowPea;
import pvz.com.entities.plants.defenders.Wallnut;
import pvz.com.entities.plants.bombs.CherryBomb;
import pvz.com.entities.plants.bombs.PotatoMine;
import pvz.com.entities.plants.shooters.Repeater;
import pvz.com.entities.components.types.PlantTypeComponent;

import pvz.com.managers.GridConfig;

public class PlantFactory {

    private static final float SUNFLOWER_SCALE_X = 0.7f;
    private static final float SUNFLOWER_SCALE_Y = 0.8f;

    private static final float PEASHOOTER_SCALE_X = 0.7f;
    private static final float PEASHOOTER_SCALE_Y = 0.8f;

    private static final float WALLNUT_SCALE_X = 0.9f;
    private static final float WALLNUT_SCALE_Y = 0.9f;

    private static final float POTATOMINE_SCALE_X = 0.7f;
    private static final float POTATOMINE_SCALE_Y = 0.6f;

    private static final float CHERRY_BOMB_WIDTH = 90f;
    private static final float CHERRY_BOMB_HEIGHT = 90f;

    private static final float SNOWPEA_SCALE_X = 0.7f;
    private static final float SNOWPEA_SCALE_Y = 0.8f;

    private static final float CHERRY_BOMB_OFFSET_X = 15f;
    private static final float CHERRY_BOMB_OFFSET_Y = 55f;

    private static final float REPEATER_SCALE_X = 0.55f;
    private static final float REPEATER_SCALE_Y = 0.55f;

    public static Plant createPlantAtCell(PlantType type, int col, int row) {
        Plant plant = null;

        switch (type) {
            case SUNFLOWER:
                plant = createSunflowerAtCell(col, row);
                break;
            case PEASHOOTER:
                plant = createPeashooterAtCell(col, row);
                break;
            case WALLNUT:
                plant = createWallnutAtCell(col, row);
                break;
            case CHERRYBOMB:
                plant = createCherryBombAtCell(col, row);
                break;
            case POTATOMINE:
                plant = createPotatoMineAtCell(col, row);
                break;
            case SNOWPEA:
                plant = createSnowPeaAtCell(col, row);
                break;
            case REPEATER:
                plant = createRepeaterAtCell(col, row);
                break;
            default:
                throw new IllegalArgumentException("Unknown plant type: " + type);
        }

        if (plant != null && !plant.hasComponent(PlantTypeComponent.class)) {
            plant.addComponent(new PlantTypeComponent(type));
        }

        return plant;
    }

    public static Plant createSunflowerAtCell(int col, int row) {
        float w = GridConfig.CELL_WIDTH * SUNFLOWER_SCALE_X;
        float h = GridConfig.CELL_HEIGHT * SUNFLOWER_SCALE_Y;

        float x = GridConfig.getActorXForCell(col, w);
        float y = GridConfig.getActorYForCell(row, h);

        return new SunFlower(x, y, col, row);
    }

    public static Plant createPeashooterAtCell(int col, int row) {
        float w = GridConfig.CELL_WIDTH * PEASHOOTER_SCALE_X;
        float h = GridConfig.CELL_HEIGHT * PEASHOOTER_SCALE_Y;

        float x = GridConfig.getActorXForCell(col, w);
        float y = GridConfig.getActorYForCell(row, h);

        return new Peashooter(x, y, col, row);
    }

    public static Plant createWallnutAtCell(int col, int row) {
        float w = GridConfig.CELL_WIDTH * WALLNUT_SCALE_X;
        float h = GridConfig.CELL_HEIGHT * WALLNUT_SCALE_Y;

        float x = GridConfig.getActorXForCell(col, w);
        float y = GridConfig.getActorYForCell(row, h);

        return new Wallnut(x, y, col, row);
    }

    public static Plant createCherryBombAtCell(int col, int row) {
        float cx = GridConfig.getCellCenterX(col);
        float cy = GridConfig.getCellCenterY(row);

        float x = cx - CHERRY_BOMB_WIDTH / 2f - CHERRY_BOMB_OFFSET_X;
        float y = cy - CHERRY_BOMB_HEIGHT / 2f - CHERRY_BOMB_OFFSET_Y;

        return new CherryBomb(x, y, col, row);
    }

    public static Plant createPotatoMineAtCell(int col, int row) {
        float w = GridConfig.CELL_WIDTH * POTATOMINE_SCALE_X;
        float h = GridConfig.CELL_HEIGHT * POTATOMINE_SCALE_Y;

        float x = GridConfig.getActorXForCell(col, w);
        float y = GridConfig.getActorYForCell(row, h);

        return new PotatoMine(x, y, col, row);
    }

    public static Plant createSnowPeaAtCell(int col, int row) {
        float w = GridConfig.CELL_WIDTH * SNOWPEA_SCALE_X;
        float h = GridConfig.CELL_HEIGHT * SNOWPEA_SCALE_Y;
        float x = GridConfig.getActorXForCell(col, w);
        float y = GridConfig.getActorYForCell(row, h);
        return new SnowPea(x, y, col, row);
    }

    public static Plant createRepeaterAtCell(int col, int row) {
        float w = GridConfig.CELL_WIDTH * REPEATER_SCALE_X;
        float h = GridConfig.CELL_HEIGHT * REPEATER_SCALE_Y;
        float x = GridConfig.getActorXForCell(col, w);
        float y = GridConfig.getActorYForCell(row, h);
        return new Repeater(x, y, col, row);
    }

    public static Plant createPlant(PlantType type, float x, float y, int col, int row) {
        Plant plant = null;

        switch (type) {
            case SUNFLOWER:
                plant = new SunFlower(x, y, col, row);
                break;
            case PEASHOOTER:
                plant = new Peashooter(x, y, col, row);
                break;
            case WALLNUT:
                plant = new Wallnut(x, y, col, row);
                break;
            case CHERRYBOMB:
                plant = new CherryBomb(x, y, col, row);
                break;
            case POTATOMINE:
                plant = new PotatoMine(x, y, col, row);
                break;
            case SNOWPEA:
                plant = new SnowPea(x, y, col, row);
                break;
            case REPEATER:
                plant = new Repeater(x, y, col, row);
                break;
            default:
                throw new IllegalArgumentException("Unknown plant type: " + type);
        }

        if (plant != null && !plant.hasComponent(PlantTypeComponent.class)) {
            plant.addComponent(new PlantTypeComponent(type));
        }

        return plant;
    }
}
