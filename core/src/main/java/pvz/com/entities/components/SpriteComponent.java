package pvz.com.entities.components;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class SpriteComponent {
    public Sprite sprite;


    public SpriteComponent(String texturePath) {
        this.sprite = new Sprite(new Texture(texturePath));
    }
    

    public SpriteComponent(Texture texture) {
        this.sprite = new Sprite(texture);
    }


    public SpriteComponent(TextureRegion region) {
        this.sprite = new Sprite(region);
    }


    public void setRegion(TextureRegion region) {
        float oldX = sprite.getX();
        float oldY = sprite.getY();
        float oldW = sprite.getWidth();
        float oldH = sprite.getHeight();
        
        sprite.setRegion(region);
        

        sprite.setBounds(oldX, oldY, oldW, oldH);
    }

    public void setPosition(float x, float y) {
        sprite.setPosition(x, y);
    }
}