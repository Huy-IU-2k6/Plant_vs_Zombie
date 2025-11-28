package pvz.com.entities;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Entity {

    // ID duy nhất cho mỗi Entity (debug / logging / network)
    public final String id;

    // Cờ để Systems biết khi nào nên xóa entity
    public boolean markedForRemoval = false;

    // Túi chứa tất cả component
    // Key: class của component (HealthComponent.class, PositionComponent.class,...)
    // Value: instance component
    private final Map<Class<?>, Object> components;

    public Entity() {
        this.id = UUID.randomUUID().toString();
        this.components = new HashMap<>();
    }

    // Thêm 1 component
    public <T> void addComponent(T component) {
        components.put(component.getClass(), component);
    }

    // Lấy component
    public <T> T getComponent(Class<T> componentClass) {
        return componentClass.cast(components.get(componentClass));
    }

    // Kiểm tra tồn tại component
    public boolean hasComponent(Class<?> componentClass) {
        return components.containsKey(componentClass);
    }

    // Xóa component (nếu cần)
    public void removeComponent(Class<?> componentClass) {
        components.remove(componentClass);
    }
}
