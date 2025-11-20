package pvz.com.systems;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import pvz.com.entities.Entity;
import pvz.com.entities.components.PositionComponent;
import pvz.com.entities.components.SpriteComponent;
import java.util.List;

public class RenderSystem {
    private SpriteBatch batch;

    public RenderSystem(SpriteBatch batch) {
        this.batch = batch;
    }

    public void update(List<Entity> entities) {
        batch.begin(); // Bắt đầu vẽ
        
        for (Entity entity : entities) {
            // Lấy Component ra
            SpriteComponent sprite = entity.getComponent(SpriteComponent.class);
            PositionComponent position = entity.getComponent(PositionComponent.class);

            // Chỉ vẽ những entity có đủ Hình ảnh và Vị trí
            if (sprite != null && position != null) {
                // Cập nhật vị trí của Sprite theo PositionComponent
                sprite.sprite.setPosition(position.x, position.y);
                
                // Vẽ nó
                sprite.sprite.draw(batch);
            }
        }
        
        batch.end(); // Kết thúc vẽ
    }
}