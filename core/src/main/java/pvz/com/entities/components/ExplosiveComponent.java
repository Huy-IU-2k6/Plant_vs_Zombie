package pvz.com.entities.components;

public class ExplosiveComponent {
    public int damage;          // Sát thương (Rất lớn: 1800)
    public float range;         // Phạm vi nổ (Bán kính)
    public float fuseTimer;     // Thời gian đếm ngược trước khi nổ (giây)
    public boolean isExploded;  // Đánh dấu đã nổ hay chưa

    public ExplosiveComponent(int damage, float range, float fuseTime) {
        this.damage = damage;
        this.range = range;
        this.fuseTimer = fuseTime;
        this.isExploded = false;
    }
}