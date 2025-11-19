package pvz.com.zombies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class ZombiesAreComing extends Actor {

    private Sound sound;
    private Texture texture;
    private long startTime;
    private boolean disposed = false;

    public ZombiesAreComing() {
        texture = new Texture("zombies_coming.png");
        sound = Gdx.audio.newSound(Gdx.files.internal("zombies_coming.wav"));

        sound.play();
        startTime = System.currentTimeMillis();

        setSize(texture.getWidth(), texture.getHeight());
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        long now = System.currentTimeMillis();
        if (now >= startTime + 1500) {
            remove();        // safe: removes actor from stage
        }
    }

    @Override
    public boolean remove() {
        dispose();           // dispose only when safely removed
        return super.remove();
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (!disposed && texture != null) {
            batch.draw(texture, getX(), getY(), getWidth(), getHeight());
        }
    }

    public void dispose() {
        if (!disposed) {
            if (texture != null) texture.dispose();
            if (sound != null) sound.dispose();
            disposed = true;
        }
    }
}
