package pvz.com.entities.components;
import com.badlogic.gdx.math.Rectangle;
public class BoundsComponent {
    public Rectangle bounds;

    public BoundsComponent(float x, float y, float width, float height) {
        bounds = new Rectangle(x, y, width, height);
    }

    public void update(float x, float y, float width, float height) {
        bounds.set(x, y, width, height);
    }
}
