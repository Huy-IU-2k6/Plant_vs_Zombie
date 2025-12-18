package pvz.com.logic;

import pvz.com.entities.plants.PlantType;

public interface IPlantPlacer {

    
    boolean canPlacePlant(PlantType type, int row, int col);

    
    boolean placePlant(PlantType type, int row, int col);
}
