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

    public ZombiesAreComing() {
        texture = new Texture("zombies_coming.png");     // image
        sound = Gdx.audio.newSound(Gdx.files.internal("zombies_coming.wav"));
        sound.play();                                     // play once
        startTime = System.currentTimeMillis();

        // Set size for actor (VERY IMPORTANT)
        setSize(texture.getWidth(), texture.getHeight());
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        long now = System.currentTimeMillis();
        if (now >= startTime + 1500) {     
            dispose();               // remove after 1.5 sec
            remove();
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.draw(texture, getX(), getY(), getWidth(), getHeight());
    }
    public void dispose() {
        texture.dispose();
        sound.dispose();
    }
}
