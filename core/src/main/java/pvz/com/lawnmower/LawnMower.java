package pvz.com.lawnmower;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

import pvz.com.Zombies.NormalZombie;

public class LawnMower {

    private static final float DEFAULT_SPEED = 300f; // tốc độ chạy khi kích hoạt

    private final Texture texture;
    private final Rectangle bounds;

    private float x;
    private float y;
    private float speed;

    private boolean active = false; // đã chạy hay chưa
    private boolean used = false; // đã chạy xong, không dùng nữa

    private final float worldWidth;

    public LawnMower(float startX, float startY, float worldWidth) {
        this.texture = new Texture(Gdx.files.internal("images/lawnmower.png"));
        this.worldWidth = worldWidth;

        this.x = startX;
        this.y = startY;
        this.speed = DEFAULT_SPEED;

        this.bounds = new Rectangle(x, y, texture.getWidth(), texture.getHeight());
    }

    // gọi mỗi frame
    public void update(float delta, Array<NormalZombie> zombies) {
        if (used)
            return;

        if (active) {
            x += speed * delta;
            bounds.setPosition(x, y);

            // Nếu ra khỏi màn hình thì coi như dùng xong
            if (x > worldWidth) {
                used = true;
                dispose();
                return;
            }

            // Kiểm tra va chạm với zombie
            for (NormalZombie zombie : zombies) {
                if (zombie.isDead())
                    continue; // giả sử bạn có isDead()

                if (bounds.overlaps(zombie.getBounds())) {
                    // TODO: tuỳ bạn xử lý: giết luôn, trừ máu, v.v.
                    zombie.kill(); // giả sử bạn có method này
                }
            }
        } else {
            // chưa kích hoạt: vẫn đứng yên
            bounds.setPosition(x, y);
        }
    }

    public void render(SpriteBatch batch) {
        if (used)
            return;
        batch.draw(texture, x, y);
    }

    // gọi khi zombie chạm vào / vượt qua
    public void trigger() {
        if (!used) {
            active = true;
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

    public void dispose() {
        texture.dispose();
    }
}
