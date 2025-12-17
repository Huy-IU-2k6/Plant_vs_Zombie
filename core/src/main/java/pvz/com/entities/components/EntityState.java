package pvz.com.entities.components;

public enum EntityState {
    IDLE, // Đứng yên
    WALKING, // Đang đi
    ATTACKING, // Đang tấn công/bắn
    EATING, // Đang ăn (dành cho Zombie)
    DYING, // Đang chết

    // --- THÊM CÁC TRẠNG THÁI MỚI CHO WALLNUT ---
    WALLNUT_FULL, // Đầy máu ( > 75%)
    WALLNUT_CRACKED_1, // Nứt nhẹ ( 25% - 75%)
    WALLNUT_CRACKED_2, // Nứt nặng ( < 25%)
    EXPLODING,
    UNARMED,
    GROWING,
    RISING,


    // --- POTATO MINE (chuẩn PvZ) ---
    POTATOMINE_UNARMED, // chôn dưới đất / chưa kích hoạt
    POTATOMINE_ARMING, // đang “lên” / đang sạc thời gian
    POTATOMINE_ARMED, // đã sẵn sàng nổ
    POTATOMINE_EXPLODING // đang nổ (animation nổ)
}
