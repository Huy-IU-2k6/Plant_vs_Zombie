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

    // Grid plants: [row][col]
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

    public Plant[][] getPlantGrid() {
        return plantGrid;
    }

    private Plant spawnPlant(Plant plant, int row, int col) {
        if (plant == null)
            return null;
        entities.add(plant);
        plants.add(plant);
        plantGrid[row][col] = plant;
        return plant;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (!enabled)
            return false;

        Vector3 world = camera.unproject(new Vector3(screenX, screenY, 0));
        float worldX = world.x;
        float worldY = world.y;

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
        float plantX = GridConfig.getCellOriginX(col);
        float plantY = GridConfig.getCellOriginY(row);

        Plant plant = null;

        if (button == Input.Buttons.LEFT) {
            plant = PlantFactory.createPeashooter(plantX, plantY);
        } else if (button == Input.Buttons.RIGHT) {
            plant = PlantFactory.createSunflower(plantX, plantY);
        }

        if (plant != null) {
            spawnPlant(plant, row, col);
            return true;
        }

        // trả false để HUD vẫn có thể xử lý tiếp nếu cần
        return false;
    }
}
