package pvz.com.entities.components;

public class PlantAttackComponent {
    public int damage;
    public float attackSpeed;       // Thời gian chờ giữa các loạt bắn (Cooldown chính)
    public float timer;             // Bộ đếm giờ
    public float range;
    public Class<?> projectileClass;
    public PlantDamageType damageType;

    // [MỚI] Cấu hình bắn liên thanh (Repeater)
    public int burstCount;          // Số viên bắn trong 1 loạt (Repeater = 2, Peashooter = 1)
    public float burstDelay;        // Thời gian chờ giữa các viên trong loạt (0.15s)
    public int shotsFiredInBurst;   // Đếm xem đã bắn được mấy viên rồi

    public PlantAttackComponent(int damage, float range, Class<?> projectileClass, PlantDamageType damageType, float attackSpeed) {
        this.damage = damage;
        this.range = range;
        this.projectileClass = projectileClass;
        this.damageType = damageType;
        this.attackSpeed = attackSpeed;
        
        this.timer = 0f;
        
        // Mặc định là bắn 1 viên (như Peashooter)
        this.burstCount = 1; 
        this.burstDelay = 0.0f;
        this.shotsFiredInBurst = 0;
    }

    // Hàm tiện ích để set chế độ Repeater
    public void setBurstFire(int count, float delay) {
        this.burstCount = count;
        this.burstDelay = delay;
    }
}