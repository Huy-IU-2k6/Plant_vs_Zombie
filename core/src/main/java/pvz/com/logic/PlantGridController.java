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

    // Giữ để không phá constructor cũ (không còn dùng để spawn nữa)
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

    /** screen -> world (theo camera game) */
    public Vector3 screenToWorld(float screenX, float screenY) {
        Vector3 world = new Vector3(screenX, screenY, 0f);
        camera.unproject(world);
        return world;
    }

    /**
     * Snap world position về cell gần nhất.
     * return int[]{row, col}
     */
    public int[] worldToNearestCell(float worldX, float worldY) {
        // GridConfig đã có hàm snap nearest
        int[] cell = GridConfig.worldToNearestCell(worldX, worldY);
        // cell[0]=row, cell[1]=col theo code bạn đang dùng
        return cell;
    }

    /**
     * Snap screen position về cell gần nhất (tiện cho input từ camera).
     * return int[]{row, col}
     */
    public int[] screenToNearestCell(float screenX, float screenY) {
        Vector3 world = screenToWorld(screenX, screenY);
        return worldToNearestCell(world.x, world.y);
    }

    // =========================================================
    // GRID STATE: occupied/register/unregister
    // =========================================================

    public boolean isCellOccupied(int row, int col) {
        if (!GridConfig.isInsideGrid(row, col))
            return false;
        return plantGrid[row][col] != null;
    }

    /**
     * Đánh dấu 1 ô grid đã có plant.
     * Gọi từ chỗ đặt plant thành công (PlacementController/GameWorld).
     */
    public void registerPlantAtCell(Plant plant, int row, int col) {
        if (plant == null)
            return;
        if (!GridConfig.isInsideGrid(row, col))
            return;

        plantGrid[row][col] = plant;

        // Gắn / cập nhật GridCellComponent cho plant
        if (!plant.hasComponent(GridCellComponent.class)) {
            plant.addComponent(new GridCellComponent(row, col));
        } else {
            GridCellComponent cell = plant.getComponent(GridCellComponent.class);
            cell.row = row;
            cell.col = col;
        }
    }

    /** Sạch ô khi plant chết / bị ăn… */
    public void unregisterPlantAtCell(int row, int col) {
        if (!GridConfig.isInsideGrid(row, col))
            return;
        plantGrid[row][col] = null;
    }

    // =========================================================
    // Input hook (giữ nguyên hành vi cũ)
    // =========================================================

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (!enabled)
            return false;

        // Hiện tại không xử lý click-to-place ở đây nữa.
        // Return false để event tiếp tục đi tới các InputProcessor khác.
        return false;
    }
}
