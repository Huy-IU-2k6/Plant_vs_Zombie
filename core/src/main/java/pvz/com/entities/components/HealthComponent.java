package pvz.com.entities.components;

public class HealthComponent {
    public float maxHealth;
    public float currentHealth;

    public HealthComponent(float maxHealth) {
        this.maxHealth = maxHealth;
        this.currentHealth = maxHealth;
    }

    public boolean isDead() {
        return currentHealth <= 0;
    }
}
