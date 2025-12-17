package pvz.com.items;

import java.util.EnumMap;

import pvz.com.entities.plants.PlantType;

public final class PlantCatalog {
    private static final EnumMap<PlantType, PlantDef> DEFS = new EnumMap<>(PlantType.class);

    static {
        // ===== Cards / Shop / Cooldown =====
        DEFS.put(PlantType.CHERRYBOMB, new PlantDef("images/Cards/CherryBomb.png", 150, 15f));
        DEFS.put(PlantType.PEASHOOTER, new PlantDef("images/Cards/PeaShooter.png", 100, 2.5f));
        DEFS.put(PlantType.POTATOMINE, new PlantDef("images/Cards/PotatoMine.png", 25, 10f));
        DEFS.put(PlantType.REPEATER, new PlantDef("images/Cards/Repeater.png", 200, 2.5f));
        DEFS.put(PlantType.SNOWPEA, new PlantDef("images/Cards/SnowPea.png", 175, 2.5f));
        DEFS.put(PlantType.SUNFLOWER, new PlantDef("images/Cards/SunFlower.png", 50, 2.5f));
        DEFS.put(PlantType.WALLNUT, new PlantDef("images/Cards/WallNut.png", 50, 10f));
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

    /** Tiện cho SeedBank: duyệt toàn bộ plant đã đăng ký. */
    public static Iterable<PlantType> types() {
        return DEFS.keySet();
    }
}
