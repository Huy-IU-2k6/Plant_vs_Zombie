package pvz.com.logic;

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;
import java.util.List;
import pvz.com.entities.Entity;
import pvz.com.entities.components.GridCellComponent;
import pvz.com.entities.plants.Plant;
import pvz.com.managers.GridConfig;

public class PlantGridController extends InputAdapter {

    // Grid plants: [row][col]
    private final Plant[][] plantGrid;

    // Unused lists kept to avoid breaking constructor signature
    @SuppressWarnings("unused")
    private final List<Entity> entities;
    @SuppressWarnings("unused")
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

    // =========================================================
    // CONVERT: screen/world -> cell
    // =========================================================

    public Vector3 screenToWorld(float screenX, float screenY) {
        Vector3 world = new Vector3(screenX, screenY, 0f);
        camera.unproject(world);
        return world;
    }

    public int[] worldToNearestCell(float worldX, float worldY) {
        return GridConfig.worldToNearestCell(worldX, worldY);
    }

    public int[] screenToNearestCell(float screenX, float screenY) {
        Vector3 world = screenToWorld(screenX, screenY);
        return worldToNearestCell(world.x, world.y);
    }

    // =========================================================
    // GRID STATE: occupied/register/unregister
    // =========================================================

    /**
     * Retrieves the plant at a specific cell.
     * Required by ShovelController.
     */
    public Plant getPlantAt(int row, int col) {
        if (!GridConfig.isInsideGrid(row, col)) {
            return null;
        }
        return plantGrid[row][col];
    }

    public boolean isCellOccupied(int row, int col) {
        if (!GridConfig.isInsideGrid(row, col))
            return false;
        return plantGrid[row][col] != null;
    }

    public void registerPlantAtCell(Plant plant, int row, int col) {
        if (plant == null || !GridConfig.isInsideGrid(row, col))
            return;

        plantGrid[row][col] = plant;

        if (!plant.hasComponent(GridCellComponent.class)) {
            plant.addComponent(new GridCellComponent(row, col));
        } else {
            GridCellComponent cell = plant.getComponent(GridCellComponent.class);
            cell.row = row;
            cell.col = col;
        }
    }

    public void unregisterPlantAtCell(int row, int col) {
        if (!GridConfig.isInsideGrid(row, col))
            return;
        plantGrid[row][col] = null;
    }

    /**
     * Marks a plant for removal and clears it from the grid.
     */
    public void removePlant(int row, int col) {
        if (!GridConfig.isInsideGrid(row, col))
            return;

        Plant plant = plantGrid[row][col];
        if (plant != null) {
            // FIX: Changed .add() to .addComponent() to match register logic
            plant.markedForRemoval = true;
            // FIX: Use unregister method to ensure grid is cleared cleanly
            unregisterPlantAtCell(row, col);
        }
    }

    // =========================================================
    // Input hook
    // =========================================================

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        // Return false to let event propagate
        return false;
    }
}
