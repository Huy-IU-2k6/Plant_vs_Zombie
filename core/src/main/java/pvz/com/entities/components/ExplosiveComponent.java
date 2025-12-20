package pvz.com.entities.components;

public class ExplosiveComponent {
    // ===== CẤU HÌNH (DATA) =====
    public int damage;          // Sát thương (1800 cho CherryBomb)
    public float range;         // Bán kính nổ (150f ~ 3x3 ô)
    
    // Tổng thời gian chờ nổ 
    public float fuseTime;      
    
    // ===== TRẠNG THÁI (STATE) =====
    // Đồng hồ đếm giờ nội bộ (tăng dần từ 0)
    public float timer;         
    
    // Đánh dấu đã nổ chưa (để chuyển animation sang BÙM)
    public boolean hasExploded; 

    public ExplosiveComponent(int damage, float range, float fuseTime) {
        this.damage = damage;
        this.range = range;
        this.fuseTime = fuseTime;
        
        // Khởi tạo mặc định
        this.timer = 0f;
        this.hasExploded = false;
    }
}