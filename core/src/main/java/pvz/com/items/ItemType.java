package pvz.com.items;

public enum ItemType {
    CHERRYBOMB("images/Cards/CherryBomb.png", 150, 15f), // very slow
    CHOMPER("images/Cards/Chomper.png", 150, 2.5f), // fast
    PEASHOOTER("images/Cards/PeaShooter.png", 100, 2.5f), // fast
    POTATOMINE("images/Cards/PotatoMine.png", 25, 10f), // slow
    REPEATER("images/Cards/Repeater.png", 200, 2.5f), // fast
    SNOWPEA("images/Cards/SnowPea.png", 175, 2.5f), // fast
    SUNFLOWER("images/Cards/SunFlower.png", 50, 2.5f), // fast
    WALLNUT("images/Cards/WallNut.png", 50, 10f); // slow

    public final String iconPath; // đường dẫn tem (card)
    public final int cost; // giá sun
    public final float cooldown; // thời gian hồi (giây)

    ItemType(String iconPath, int cost, float cooldown) {
        this.iconPath = iconPath;
        this.cost = cost;
        this.cooldown = cooldown;
    }
}
