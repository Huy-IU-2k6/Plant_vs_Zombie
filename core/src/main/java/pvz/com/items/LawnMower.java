package pvz.com.items;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

import pvz.com.entities.Zombies.BaseZombie;

public class LawnMower {

    private static final float DEFAULT_SPEED = 500f;

    private static final float WIDTH = 110f;
    private static final float HEIGHT = 90f;

    private static final float HITBOX_W = 90f;
    private static final float HITBOX_H = 70f;

    private final Texture idleTexture;
    private final Texture activeTexture;
    private Texture currentTexture;

    private final Rectangle bounds;

    private float x, y;
    private float speed;
    private boolean active = false;
    private boolean used = false;
    private final float worldWidth;

    public LawnMower(float startX, float startY, float worldWidth, Texture idle, Texture active) {
        this.worldWidth = worldWidth;
        this.x = startX;
        this.y = startY;
        this.speed = DEFAULT_SPEED;

        this.idleTexture = idle;
        this.activeTexture = active;
        this.currentTexture = idleTexture;

        this.bounds = new Rectangle(x + 10, y + 5, HITBOX_W, HITBOX_H);
    }

    public void update(float delta, Array<BaseZombie> zombies) {
        if (used)
            return;

        if (!active) {
            for (BaseZombie z : zombies) {
                if (z.isDead())
                    continue;

                if (bounds.overlaps(z.getBounds())) {
                    trigger();
                    break;
                }
            }
        }

        if (active) {
            x += speed * delta;

            bounds.setPosition(x + 10, y + 5);

            for (BaseZombie z : zombies) {
                if (z.isDead())
                    continue;

                if (bounds.overlaps(z.getBounds())) {

                    z.killByMower();
                }
            }

            if (x > worldWidth + 100) {
                active = false;
                used = true;
            }
        }
    }

    public void render(SpriteBatch batch) {
        if (used)
            return;

        batch.draw(currentTexture, x, y, WIDTH, HEIGHT);
    }

    public void trigger() {
        if (!used && !active) {
            active = true;
            currentTexture = activeTexture;

        }
    }

    public boolean isUsed() {
        return used;
    }

}
