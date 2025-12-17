package pvz.com.items;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

import pvz.com.entities.Zombies.Zombies;

public class LawnMower {

    private static final float DEFAULT_SPEED = 500f;

    // Kích thước hiển thị (Vẽ)
    private static final float WIDTH = 110f;
    private static final float HEIGHT = 90f;

    // Kích thước Hitbox (Va chạm) - Nhỏ hơn hình vẽ chút cho chuẩn
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

    // Constructor nhận Texture thay vì tự load
    public LawnMower(float startX, float startY, float worldWidth, Texture idle, Texture active) {
        this.worldWidth = worldWidth;
        this.x = startX;
        this.y = startY;
        this.speed = DEFAULT_SPEED;

        this.idleTexture = idle;
        this.activeTexture = active;
        this.currentTexture = idleTexture;

        // Tạo hitbox căn giữa hình ảnh
        this.bounds = new Rectangle(x + 10, y + 5, HITBOX_W, HITBOX_H);
    }

    public void update(float delta, Array<Zombies> zombies) {
        if (used)
            return;

        // 1. Logic kích hoạt
        if (!active) {
            for (Zombies z : zombies) {
                if (z.isDead())
                    continue;
                // Chỉ check va chạm nếu hitbox chạm nhau
                if (bounds.overlaps(z.getBounds())) {
                    trigger();
                    break;
                }
            }
        }

        // 2. Logic chạy và giết
        if (active) {
            x += speed * delta;

            // Cập nhật hitbox theo vị trí mới
            bounds.setPosition(x + 10, y + 5);

            for (Zombies z : zombies) {
                if (z.isDead())
                    continue;

                // Va chạm khi đang chạy -> Giết
                if (bounds.overlaps(z.getBounds())) {
                    // [LƯU Ý] Đảm bảo class Zombies có hàm này
                    z.killByMower();
                }
            }

            // Ra khỏi màn hình -> Hủy
            if (x > worldWidth + 100) {
                active = false;
                used = true;
            }
        }
    }

    public void render(SpriteBatch batch) {
        if (used)
            return;
        // Vẽ texture
        batch.draw(currentTexture, x, y, WIDTH, HEIGHT);
    }

    public void trigger() {
        if (!used && !active) {
            active = true;
            currentTexture = activeTexture;
            // Có thể play sound ở đây nếu muốn
        }
    }

    public boolean isUsed() {
        return used;
    }

    // Không cần hàm dispose ở đây nữa vì Texture do Controller quản lý
}
