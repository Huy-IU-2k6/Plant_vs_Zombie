<<<<<<< HEAD:core/src/main/java/pvz/com/entities/Zombies/ZombiesAreComing.java
package pvz.com.entities.Zombies;
=======
package pvz.com.Zombies;
>>>>>>> origin:core/src/main/java/pvz/com/Zombies/ZombiesAreComing.java

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class ZombiesAreComing extends Actor {

    private static final float DISPLAY_TIME = 1.5f; // thời gian hiển thị (giây)

    private final Sound sound;
    private float elapsedTime = 0f;

    public ZombiesAreComing() {
        // load asset
        sound = Gdx.audio.newSound(Gdx.files.internal("assets/sounds/zombies_are_coming.wav"));

        // play sound một lần
        sound.play();
    }

    @Override
    public void act(float delta) {
        super.act(delta);

<<<<<<< HEAD:core/src/main/java/pvz/com/entities/Zombies/ZombiesAreComing.java
        long now = System.currentTimeMillis();

        // Remove after 1.5 seconds
        if (now - startTime >= 1500) {
            remove();     // remove from stage safely
=======
        elapsedTime += delta;

        // Remove sau 1.5s
        if (elapsedTime >= DISPLAY_TIME) {
            remove(); // sẽ gọi override remove() phía dưới
>>>>>>> origin:core/src/main/java/pvz/com/Zombies/ZombiesAreComing.java
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
    }

    @Override
    public boolean remove() {
        // Khi actor bị remove khỏi Stage thì dọn luôn asset
        dispose();
        return super.remove();
    }

    public void dispose() {
        if (sound != null) {
            sound.dispose();
        }
    }
}

