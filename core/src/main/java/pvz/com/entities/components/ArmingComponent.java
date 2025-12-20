package pvz.com.entities.components;

public class ArmingComponent {
    public float armingTimer;
    public boolean isArmed;

    public ArmingComponent(float timeToArm) {
        this.armingTimer = timeToArm;
        this.isArmed = false;
    }
}