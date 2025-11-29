package pvz.com.items;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

import pvz.com.entities.Zombies.NormalZombie;

public class LawnMower {

    private static final float DEFAULT_SPEED = 300f;
    private static final float FIXED_WIDTH = 80f;
    private static final float FIXED_HEIGHT = 60f;

    private final Texture idleTexture;
    private final Texture activeTexture;
    private Texture currentTexture; // texture đang dùng để vẽ

    private final Rectangle bounds;

    private float x;
    private float y;
    private float speed;

    private boolean active = false;
    private boolean used = false;

    private final float worldWidth;

    public LawnMower(float startX, float startY, float worldWidth) {
        this.idleTexture = new Texture(
                Gdx.files.internal("assets/images/items/lawnMower_Idle.png"));
        this.activeTexture = new Texture(
                Gdx.files.internal("assets/images/items/lawnMower_Active.gif"));

        this.currentTexture = idleTexture; // mặc định đứng yên

        this.worldWidth = worldWidth;
        this.x = startX;
        this.y = startY;
        this.speed = DEFAULT_SPEED;

        this.bounds = new Rectangle(x, y, FIXED_WIDTH, FIXED_HEIGHT);
    }

    public void update(float delta, Array<NormalZombie> zombies) {
        if (used)
            return;

        // Chưa active thì check va chạm để trigger
        if (!active) {
            for (NormalZombie z : zombies) {
                if (z.isDead())
                    continue;
                if (bounds.overlaps(z.getBounds())) {
                    trigger();
                    break;
                }
            }
        }

        // Nếu đã active thì chạy + giết zombie trên đường
        if (active) {
            x += speed * delta;
            bounds.setPosition(x, y);

            for (NormalZombie z : zombies) {
                if (z.isDead())
                    continue;
                if (bounds.overlaps(z.getBounds())) {
                    z.instantKillByMower(); // hoặc z.takeDamage(9999);
                }
            }

            // chạy khỏi màn thì đánh dấu used
            if (x > worldWidth + currentTexture.getWidth()) {
                active = false;
                used = true;
            }
        }
    }

    public void render(SpriteBatch batch) {
        if (used)
            return;
        batch.draw(currentTexture, x, y, FIXED_WIDTH, FIXED_HEIGHT);
    }

    public void trigger() {
        if (!used && !active) {
            active = true;
            currentTexture = activeTexture; // 👉 đổi qua ảnh đang chạy
        }
    }

    public boolean isActive() {
        return active;
    }

    public boolean isUsed() {
        return used;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
        bounds.setPosition(x, y);
    }

    public void dispose() {
        idleTexture.dispose();
        activeTexture.dispose();
    }
}
