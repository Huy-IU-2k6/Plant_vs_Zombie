package pvz.com.entities.components;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import java.util.HashMap;
import java.util.Map;

public class AnimationComponent {
    // Map lưu trữ animation tương ứng với từng trạng thái (ví dụ: IDLE -> animation lắc lư)
    public Map<EntityState, Animation<TextureRegion>> animations;

    public AnimationComponent() {
        animations = new HashMap<>();
    }

    /**
     * Thêm một animation cho một trạng thái cụ thể.
     * @param state Trạng thái (ví dụ: EntityState.IDLE)
     * @param animation Animation của LibGDX (chứa danh sách frames)
     */
    public void addAnimation(EntityState state, Animation<TextureRegion> animation) {
        animations.put(state, animation);
    }

    /**
     * Lấy animation dựa trên trạng thái hiện tại.
     */
    public Animation<TextureRegion> getAnimation(EntityState state) {
        return animations.get(state);
    }
}