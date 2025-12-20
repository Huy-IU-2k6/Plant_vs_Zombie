package pvz.com.entities.Zombies;

import com.badlogic.gdx.math.Rectangle;

public class ZombieBounds {
    private final Rectangle bounds = new Rectangle();
    private static final float WIDTH_RATIO = 0.6f;
    private static final float HEIGHT_RATIO = 0.85f;

    public ZombieBounds(float w, float h) {

    }

    public void update(float x, float y, float w, float h) {
        float hitW = w * WIDTH_RATIO;
        float hitH = h * HEIGHT_RATIO;
        float hitX = x + (w - hitW) / 2f;
        bounds.set(hitX, y, hitW, hitH);
    }

    public Rectangle getBounds() { return bounds; }
}