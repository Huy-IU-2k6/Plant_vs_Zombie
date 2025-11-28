package pvz.com.entities.components;

public enum EntityState {
    IDLE,       // Đứng yên
    WALKING,    // Đang đi
    ATTACKING,  // Đang tấn công/bắn
    EATING,     // Đang ăn (dành cho Zombie)
    DYING       // Đang chết
}