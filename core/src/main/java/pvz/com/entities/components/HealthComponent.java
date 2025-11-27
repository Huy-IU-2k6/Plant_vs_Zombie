package pvz.com.entities.components;

public class HealthComponent {
    public int maxHealth;
    public int currentHealth;

    public HealthComponent(int maxHealth) {
        this.maxHealth = maxHealth;
        this.currentHealth = maxHealth;
    }

    public boolean isDead() {
        return currentHealth <= 0;
    }
}
