package pvz.com.entities.components;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
public class SpriteComponent {
    public Texture texture;
    public Sprite sprite;

    public SpriteComponent(String texturePath) {
        this.texture = new Texture(texturePath);
        this.sprite = new Sprite(texture);
        }
    public void setPosition(float x, float y) {
        sprite.setPosition(x, y);
    }
}

