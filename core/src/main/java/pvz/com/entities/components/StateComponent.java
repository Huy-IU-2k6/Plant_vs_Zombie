package pvz.com.entities.components;

public class StateComponent {
    private EntityState currentState;
    public float timeInState; // Thời gian đã ở trong trạng thái này (để tính animation)

    public StateComponent(EntityState startState) {
        this.currentState = startState;
        this.timeInState = 0f;
    }

    public EntityState get() {
        return currentState;
    }

    public void set(EntityState newState) {
        // Chỉ reset thời gian nếu trạng thái thực sự thay đổi
        if (this.currentState != newState) {
            this.currentState = newState;
            this.timeInState = 0f;
        }
    }
}