package pvz.com.entities;

import java.util.HashMap;
import java.util.Map;

public class Entity {
    // Một cái Map để lưu trữ tất cả component.
    // Key: Tên class (ví dụ: HealthComponent.class)
    // Value: Bản thân object component đó
    private Map<Class<?>, Object> components;

    public Entity() {
        components = new HashMap<>();
    }

    // 1. Hàm thêm Component vào túi
    public <T> void addComponent(T component) {
        components.put(component.getClass(), component);
    }

    // 2. Hàm lấy Component ra để dùng
    // Cú pháp <T> T giúp bạn không cần ép kiểu thủ công (casting)
    public <T> T getComponent(Class<T> componentClass) {
        return componentClass.cast(components.get(componentClass));
    }
    
    // 3. Hàm kiểm tra xem có Component đó không (Optional)
    public boolean hasComponent(Class<?> componentClass) {
        return components.containsKey(componentClass);
    }
}