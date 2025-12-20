package pvz.com.entities;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Entity {


    public final String id;


    public boolean markedForRemoval = false;




    private final Map<Class<?>, Object> components;

    public Entity() {
        this.id = UUID.randomUUID().toString();
        this.components = new HashMap<>();
    }


    public <T> void addComponent(T component) {
        components.put(component.getClass(), component);
    }


    public <T> T getComponent(Class<T> componentClass) {
        return componentClass.cast(components.get(componentClass));
    }


    public boolean hasComponent(Class<?> componentClass) {
        return components.containsKey(componentClass);
    }


    public void removeComponent(Class<?> componentClass) {
        components.remove(componentClass);
    }
}
