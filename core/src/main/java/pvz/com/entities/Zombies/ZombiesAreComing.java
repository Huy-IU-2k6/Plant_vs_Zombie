package pvz.com.entities.Zombies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class ZombiesAreComing extends Actor {

    private static final float DISPLAY_TIME = 1.5f;

    private final Sound sound;
    private float elapsedTime = 0f;

    public ZombiesAreComing() {

        sound = Gdx.audio.newSound(Gdx.files.internal("sounds/zombies_are_coming.wav"));


        sound.play();
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        elapsedTime += delta;


        if (elapsedTime >= DISPLAY_TIME) {
            remove();
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
    }

    @Override
    public boolean remove() {

        dispose();
        return super.remove();
    }

    public void dispose() {
        if (sound != null) {
            sound.dispose();
        }
    }
}
