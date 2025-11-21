package pvz.com.Zombies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class ZombiesAreComing extends Actor {

    private static final float DISPLAY_TIME = 1.5f; // thời gian hiển thị (giây)

    private final Sound sound;
    private final Texture texture;
    private float elapsedTime = 0f;

    public ZombiesAreComing() {
        // load asset
        texture = new Texture(Gdx.files.internal("assets/sounds/zombies_coming.png"));
        sound = Gdx.audio.newSound(Gdx.files.internal("assets/sounds/zombies_are_coming.wav"));

        // play sound một lần
        sound.play();

        // set kích thước actor khớp texture
        setSize(texture.getWidth(), texture.getHeight());
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        elapsedTime += delta;

        // Remove sau 1.5s
        if (elapsedTime >= DISPLAY_TIME) {
            remove(); // sẽ gọi override remove() phía dưới
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (texture != null) {
            batch.setColor(1f, 1f, 1f, parentAlpha); // đảm bảo ăn alpha của parent
            batch.draw(texture, getX(), getY(), getWidth(), getHeight());
        }
    }

    @Override
    public boolean remove() {
        // Khi actor bị remove khỏi Stage thì dọn luôn asset
        dispose();
        return super.remove();
    }

    public void dispose() {
        if (texture != null) {
            texture.dispose();
        }
        if (sound != null) {
            sound.dispose();
        }
    }
}
