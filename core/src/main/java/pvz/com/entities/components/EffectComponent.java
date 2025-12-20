package pvz.com.entities.components;

public class EffectComponent {
    public boolean isSlowed;
    public float slowFactor;
    public float effectDuration;
    public float originalSpeed;

    public EffectComponent() {
        this.isSlowed = false;
        this.slowFactor = 1f;
        this.effectDuration = 0f;
        this.originalSpeed = 0f;
    }
    
    public void applySlow(float duration, float factor, float currentBaseSpeed) {
        this.isSlowed = true;
        this.effectDuration = duration;
        this.slowFactor = factor;
        if (this.originalSpeed == 0) {
            this.originalSpeed = currentBaseSpeed;
        }
    }
}