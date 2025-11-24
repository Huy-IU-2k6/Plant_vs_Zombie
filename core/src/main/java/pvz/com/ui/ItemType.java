package pvz.com.ui;

public enum ItemType {
    CHERRYBOMB("assets/images/Cards/CherryBomb.png", 150, 50f), // very slow
    CHOMPER("assets/images/Cards/Chomper.png", 150, 7.5f), // fast
    JALAPENO("assets/images/Cards/Jalapeno.png", 125, 50f), // very slow
    PEASHOOTER("assets/images/Cards/PeaShooter.png", 100, 7.5f), // fast
    POTATOMINE("assets/images/Cards/PotatoMine.png", 25, 30f), // slow
    REPEATER("assets/images/Cards/Repeater.png", 200, 7.5f), // fast
    SNOWPEA("assets/images/Cards/SnowPea.png", 175, 7.5f), // fast
    SUNFLOWER("assets/images/Cards/SunFlower.png", 50, 7.5f), // fast
    WALLNUT("assets/images/Cards/WallNut.png", 50, 30f); // slow

    public final String iconPath; // đường dẫn tem (card)
    public final int cost; // giá sun
    public final float cooldown; // thời gian hồi (giây)

    ItemType(String iconPath, int cost, float cooldown) {
        this.iconPath = iconPath;
        this.cost = cost;
        this.cooldown = cooldown;
    }
}
