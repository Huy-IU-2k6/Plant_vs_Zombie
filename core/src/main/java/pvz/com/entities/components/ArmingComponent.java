package pvz.com.entities.components;

public class ArmingComponent {
    public float armingTimer;  // Thời gian đếm ngược (khoảng 14s trong game gốc)
    public boolean isArmed;    // Đã sẵn sàng chưa?

    public ArmingComponent(float timeToArm) {
        this.armingTimer = timeToArm;
        this.isArmed = false;
    }
}