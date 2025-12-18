package pvz.com.logic;

import pvz.com.entities.plants.Plant;

public class ShovelController {

    private final PlantGridController grid;

    public ShovelController(PlantGridController grid) {
        this.grid = grid;
    }

    public void tryRemovePlant(int row, int col) {
        
        Plant plant = grid.getPlantAt(row, col);

        if (plant != null) {
            grid.removePlant(row, col);
        }
    }
}
