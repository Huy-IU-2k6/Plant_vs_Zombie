package pvz.com.entities.components;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class SpriteComponent {
    public Texture texture;
    public Sprite sprite;

    // Dùng cho static image
    public SpriteComponent(String texturePath) {
        this.texture = new Texture(texturePath);
        this.sprite = new Sprite(texture);
    }

    // Dùng cho animation frame (TextureRegion)
    public SpriteComponent(TextureRegion region) {
        this.texture = region.getTexture(); // tham chiếu lại texture gốc
        this.sprite = new Sprite(region);
    }

    public void setPosition(float x, float y) {
        sprite.setPosition(x, y);
    }
}
