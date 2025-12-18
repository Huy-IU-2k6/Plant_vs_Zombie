package pvz.com.systems;

import java.util.List;
import pvz.com.entities.Entity;
import pvz.com.entities.plants.Plant;
import pvz.com.logic.PlantGridController;
import pvz.com.entities.components.GridCellComponent;

public class CleanupSystem {
    private final List<Entity> entities;
    private final List<Plant> plants;
    private final PlantGridController grid;

    public CleanupSystem(List<Entity> entities, List<Plant> plants, PlantGridController grid) {
        this.entities = entities;
        this.plants = plants;
        this.grid = grid;
    }

    public void update() {
        // Clear grid for plants that will be removed
        for (Plant p : plants) {
            if (p != null && p.markedForRemoval) {
                GridCellComponent cell = p.getComponent(GridCellComponent.class);
                if (cell != null) {
                    grid.unregisterPlantAtCell(cell.row, cell.col);
                }
            }
        }

        entities.removeIf(e -> e.markedForRemoval);
        plants.removeIf(p -> p == null || p.markedForRemoval);
    }
}
