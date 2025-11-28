package pvz.com.entities.components;

public class RemoveComponent {
    public boolean shouldRemove;
    
    public RemoveComponent() {
        this.shouldRemove = true; // Mặc định nếu add component này vào nghĩa là muốn xóa
    }
}