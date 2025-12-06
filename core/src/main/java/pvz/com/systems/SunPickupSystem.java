package pvz.com.systems;

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;

import java.util.ArrayList;
import java.util.List;

import pvz.com.entities.Entity;
import pvz.com.entities.components.BoundsComponent;
import pvz.com.entities.components.SunPickupComponent;
import pvz.com.logic.GameWorld;

public class SunPickupSystem extends InputAdapter {

    private final List<Entity> entities;
    private final OrthographicCamera camera;
    private final GameWorld gameWorld;

    public SunPickupSystem(List<Entity> entities,
            OrthographicCamera camera,
            GameWorld gameWorld) {
        this.entities = entities;
        this.camera = camera;
        this.gameWorld = gameWorld;
    }

    // Sun hết hạn thì tự biến mất
    public void update(float deltaTime) {
        List<Entity> toRemove = new ArrayList<>();

        for (Entity e : entities) {
            if (!e.hasComponent(SunPickupComponent.class))
                continue;

            SunPickupComponent sun = e.getComponent(SunPickupComponent.class);
            sun.aliveTime += deltaTime;

            if (sun.aliveTime >= sun.lifeTime) {
                toRemove.add(e);
            }
        }

        entities.removeAll(toRemove);
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        // chuyển toạ độ screen -> world
        Vector3 world = new Vector3(screenX, screenY, 0);
        camera.unproject(world);

        List<Entity> toRemove = new ArrayList<>();

        for (Entity e : entities) {
            if (!e.hasComponent(SunPickupComponent.class)
                    || !e.hasComponent(BoundsComponent.class)) {
                continue;
            }

            BoundsComponent bounds = e.getComponent(BoundsComponent.class);

            if (bounds.bounds.contains(world.x, world.y)) {
                SunPickupComponent sun = e.getComponent(SunPickupComponent.class);

                // Cộng sun vào GameWorld (thực chất là HUD)
                gameWorld.addSun(sun.amount);

                toRemove.add(e);
            }
        }

        entities.removeAll(toRemove);
        // đã xử lý event, trả về true
        return !toRemove.isEmpty();
    }
}
