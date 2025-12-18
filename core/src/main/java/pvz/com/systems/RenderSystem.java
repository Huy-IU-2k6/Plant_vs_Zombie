package pvz.com.systems;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import pvz.com.entities.Entity;
import pvz.com.entities.components.PositionComponent;
import pvz.com.entities.components.SpriteComponent;
import java.util.List;

import pvz.com.entities.components.SizeComponent;

public class RenderSystem {
    private SpriteBatch batch;

    public RenderSystem(SpriteBatch batch) {
        this.batch = batch;
    }

    public void update(List<Entity> entities) {
        batch.begin();

        for (Entity entity : entities) {
            if (entity.markedForRemoval)
                continue;

            SpriteComponent sprite = entity.getComponent(SpriteComponent.class);
            PositionComponent position = entity.getComponent(PositionComponent.class);
            SizeComponent size = entity.getComponent(SizeComponent.class);

            if (sprite != null && position != null) {
                if (size != null) {
                    sprite.sprite.setBounds(
                            position.x,
                            position.y,
                            size.width,
                            size.height);
                } else {
                    sprite.sprite.setPosition(position.x, position.y);
                }

                sprite.sprite.draw(batch);
            }
        }

        batch.end();

    }
}
