package pvz.com.entities.Zombies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class ZombiesAreComing extends Actor {

    private Sound sound;
    private Texture texture;
    private long startTime;

    public ZombiesAreComing() {
        texture = new Texture("zombies_coming.png");   // image
        sound = Gdx.audio.newSound(Gdx.files.internal("zombies_are_coming.wav"));

        sound.play();                                   // play sound once
        startTime = System.currentTimeMillis();

        // Set actor size to match texture (important)
        setSize(texture.getWidth(), texture.getHeight());
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        long now = System.currentTimeMillis();

        // Remove after 1.5 seconds
        if (now - startTime >= 1500) {
            remove();     // remove from stage safely
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (texture != null) {
            batch.draw(texture, getX(), getY(), getWidth(), getHeight());
        }
    }

    public void dispose() {
        // Clean up
        if (texture != null) {
            texture.dispose();
            texture = null;
        }

        if (sound != null) {
            sound.dispose();
            sound = null;
        }
    }
}

