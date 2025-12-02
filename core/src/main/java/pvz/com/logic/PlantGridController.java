package pvz.com.logic;

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;

import java.util.List;

import pvz.com.entities.Entity;
import pvz.com.entities.plants.Plant;
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

    /**
     * Đánh dấu 1 ô grid đã có plant.
     * Gọi từ chỗ khác (ví dụ GameScreen) sau khi spawn plant thành công.
     */
    public void registerPlantAtCell(Plant plant, int row, int col) {
        if (plant == null)
            return;
        if (!GridConfig.isInsideGrid(row, col))
            return;

        plantGrid[row][col] = plant;
    }

    /**
     * Nếu cần sạch ô khi plant chết / bị ăn…
     */
    public void unregisterPlantAtCell(int row, int col) {
        if (!GridConfig.isInsideGrid(row, col))
            return;
        plantGrid[row][col] = null;
    }

    public boolean isCellOccupied(int row, int col) {
        // Nếu ngoài grid thì coi như không bị chiếm
        if (!GridConfig.isInsideGrid(row, col)) {
            return false;
        }
        return plantGrid[row][col] != null;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        // Trước đây xử lý click trái/phải để trồng cây ở đây.
        // Bây giờ trồng cây đã chuyển qua drag & drop card,
        // nên không còn xử lý gì trong touchDown nữa.
        if (!enabled)
            return false;

        // return false để event tiếp tục đi tới các InputProcessor khác
        // (vd: SunPickupSystem nhặt sun).
        return false;
    }
}
