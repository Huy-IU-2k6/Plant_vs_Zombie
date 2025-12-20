package pvz.com.items;

import java.util.EnumMap;
import pvz.com.entities.plants.PlantType;

public final class PlantCatalog {
    private static final EnumMap<PlantType, PlantDef> DEFS = new EnumMap<>(PlantType.class);

    static {
        
        DEFS.put(PlantType.CHERRYBOMB, new PlantDef("images/cards/CherryBomb.png", 150, 15f));
        DEFS.put(PlantType.PEASHOOTER, new PlantDef("images/cards/PeaShooter.png", 100, 2.5f));
        DEFS.put(PlantType.POTATOMINE, new PlantDef("images/cards/PotatoMine.png", 25, 10f));
        DEFS.put(PlantType.REPEATER, new PlantDef("images/cards/Repeater.png", 200, 2.5f));
        DEFS.put(PlantType.SNOWPEA, new PlantDef("images/cards/SnowPea.png", 175, 2.5f));
        DEFS.put(PlantType.SUNFLOWER, new PlantDef("images/cards/SunFlower.png", 50, 2.5f));
        DEFS.put(PlantType.WALLNUT, new PlantDef("images/cards/WallNut.png", 50, 10f));
    }

    private PlantCatalog() {
    }

    public static PlantDef def(PlantType type) {
        PlantDef d = DEFS.get(type);
        if (d == null) {
            throw new IllegalArgumentException("Missing PlantDef for type: " + type);
        }
        return d;
    }

    
    public static Iterable<PlantType> types() {
        return DEFS.keySet();
    }
}
