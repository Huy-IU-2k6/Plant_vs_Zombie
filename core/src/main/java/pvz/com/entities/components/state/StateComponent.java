package pvz.com.entities.components.state;

public class StateComponent {
    private EntityState currentState;
    public float timeInState;

    public StateComponent(EntityState startState) {
        this.currentState = startState;
        this.timeInState = 0f;
    }

    public EntityState get() {
        return currentState;
    }

    public void set(EntityState newState) {

        if (this.currentState != newState) {
            this.currentState = newState;
            this.timeInState = 0f;
        }
    }
}
