package pvz.com.entities;

import java.util.HashMap;
import java.util.UUID;

public class Entity {
    // ID duy nhất cho mỗi Entity (để debug hoặc xử lý logic mạng sau này)
    public String id;
    
    // Map lưu trữ components: Key là Class, Value là Object component
    private HashMap<Class<?>, Object> components;
    
    // Cờ đánh dấu để System xóa entity này khỏi game (ví dụ khi chết)
    public boolean markedForRemoval = false;

    public Entity() {
        this.id = UUID.randomUUID().toString();
        this.components = new HashMap<>();
    }

    // Thêm một component vào Entity
    public <T> void addComponent(T component) {
        components.put(component.getClass(), component);
    }

    // Lấy một component ra để sử dụng
    // Ví dụ: position = entity.getComponent(PositionComponent.class);
    public <T> T getComponent(Class<T> componentClass) {
        return componentClass.cast(components.get(componentClass));
    }
    
    // Kiểm tra xem Entity có component này không
    public boolean hasComponent(Class<?> componentClass) {
        return components.containsKey(componentClass);
    }
    
    // Xóa component (ít dùng nhưng cần thiết)
    public void removeComponent(Class<?> componentClass) {
        components.remove(componentClass);
    }
}