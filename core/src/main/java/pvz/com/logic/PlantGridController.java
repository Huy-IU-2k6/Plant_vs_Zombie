package pvz.com.logic;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;

import java.util.List;

import pvz.com.entities.Entity;
import pvz.com.entities.plants.Plant;
import pvz.com.factories.PlantFactory;
import pvz.com.managers.GridConfig;

public class PlantGridController extends InputAdapter {

    // Grid plants: [row][col] – để không cho đặt 2 cây trùng 1 ô
    private final Plant[][] plantGrid;

    private final List<Entity> entities;
    private final List<Plant> plants;
    private final OrthographicCamera camera;

    private boolean enabled = false;

    public PlantGridController(List<Entity> entities,
            List<Plant> plants,
            OrthographicCamera camera) {
        this.entities = entities;
        this.plants = plants;
        this.camera = camera;
        this.plantGrid = new Plant[GridConfig.ROWS][GridConfig.COLS];
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /** Thêm 1 plant vào ECS world và trả về plant. */
    private Plant spawnPlant(Plant plant) {
        if (plant == null)
            return null;
        entities.add(plant);
        plants.add(plant);
        return plant;
    }

    /** Demo: đặt sẵn vài cây lên grid cho đúng hàng/cột. */
    public void initDemoPlants() {
        int[][] demoCells = {
                { 0, 1 },
                { 1, 1 },
                { 2, 1 }
        };

        // Sunflower
        {
            int row = demoCells[0][0];
            int col = demoCells[0][1];
            float x = GridConfig.getCellCenterX(col);
            float y = GridConfig.getCellCenterY(row);
            Plant p = spawnPlant(PlantFactory.createSunflower(x, y));
            plantGrid[row][col] = p;
        }

        // Peashooter
        {
            int row = demoCells[1][0];
            int col = demoCells[1][1];
            float x = GridConfig.getCellCenterX(col);
            float y = GridConfig.getCellCenterY(row);
            Plant p = spawnPlant(PlantFactory.createPeashooter(x, y));
            plantGrid[row][col] = p;
        }

        // Wallnut
        {
            int row = demoCells[2][0];
            int col = demoCells[2][1];
            float x = GridConfig.getCellCenterX(col);
            float y = GridConfig.getCellCenterY(row);
            Plant p = spawnPlant(PlantFactory.createWallnut(x, y));
            plantGrid[row][col] = p;
        }
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (!enabled) {
            return false;
        }

        // Convert screen -> world
        Vector3 world = camera.unproject(new Vector3(screenX, screenY, 0));
        float worldX = world.x;
        float worldY = world.y;

        // Chuyển worldX, worldY -> col, row theo GridConfig
        int col = GridConfig.worldToCol(worldX);
        int row = GridConfig.worldToRow(worldY);

        if (!GridConfig.isInsideGrid(row, col)) {
            // click ngoài lawn
            return false;
        }

        // Nếu ô này đã có plant thì không đặt nữa
        if (plantGrid[row][col] != null) {
            return false;
        }

        // Tâm ô
        float plantX = GridConfig.getCellCenterX(col);
        float plantY = GridConfig.getCellCenterY(row);

        Plant plant = null;

        if (button == Input.Buttons.LEFT) {
            plant = PlantFactory.createPeashooter(plantX, plantY);
        } else if (button == Input.Buttons.RIGHT) {
            plant = PlantFactory.createSunflower(plantX, plantY);
        }

        if (plant != null) {
            Plant spawned = spawnPlant(plant);
            plantGrid[row][col] = spawned;
            return true;
        }

        // trả false để InputMultiplexer cho các processor khác xử lý tiếp nếu cần
        return false;
    }
}
