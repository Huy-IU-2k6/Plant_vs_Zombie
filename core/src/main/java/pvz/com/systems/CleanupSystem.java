package pvz.com.systems;

import java.util.List;
import pvz.com.entities.Entity;

public class CleanupSystem {

    private final List<Entity> entities;

    public CleanupSystem(List<Entity> entities) {
        this.entities = entities;
    }

    public void update() {
        entities.removeIf(e -> e.markedForRemoval);
    }
}
