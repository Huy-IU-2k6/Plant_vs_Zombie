package pvz.com.entities.Zombies;

import com.badlogic.gdx.math.Rectangle;

public class ZombieBounds {

    // ===== HITBOX =====
    private final Rectangle hitBox;

    // tỉ lệ hitbox so với sprite
    private static final float HITBOX_WIDTH_RATIO = 0.6f; // 60% chiều rộng
    private static final float HITBOX_HEIGHT_RATIO = 0.85f; // 85% chiều cao

    public ZombieBounds(float spriteWidth, float spriteHeight) {
        // khởi tạo tạm, x/y sẽ set sau
        float w = spriteWidth * HITBOX_WIDTH_RATIO;
        float h = spriteHeight * HITBOX_HEIGHT_RATIO;
        this.hitBox = new Rectangle(0, 0, w, h);
    }

    /**
     * Gọi mỗi khi zombie đổi vị trí / kích thước
     */
    public void update(float spriteX, float spriteY, float spriteWidth, float spriteHeight) {
        float w = spriteWidth * HITBOX_WIDTH_RATIO;
        float h = spriteHeight * HITBOX_HEIGHT_RATIO;

        // canh giữa theo chiều ngang
        float x = spriteX + (spriteWidth - w) / 2f;

        // bám từ chân lên
        float y = spriteY;

        hitBox.set(x, y, w, h);
    }

    public Rectangle getBounds() {
        return hitBox;
    }
}
