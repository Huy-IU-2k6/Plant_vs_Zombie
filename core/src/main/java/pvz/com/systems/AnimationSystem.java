package pvz.com.systems;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import pvz.com.entities.Entity;
import pvz.com.entities.components.AnimationComponent;
import pvz.com.entities.components.SpriteComponent;
import pvz.com.entities.components.StateComponent;

import java.util.List;

public class AnimationSystem {

    // Hàm này được gọi mỗi khung hình (trong GameScreen.render)
    public void update(List<Entity> entities, float deltaTime) {
        
        for (Entity entity : entities) {
            // 1. Lấy các nguyên liệu cần thiết
            AnimationComponent animComp = entity.getComponent(AnimationComponent.class);
            StateComponent stateComp = entity.getComponent(StateComponent.class);
            SpriteComponent spriteComp = entity.getComponent(SpriteComponent.class);

            // Nếu thiếu 1 trong 3 cái thì bỏ qua (ví dụ cục đá không có animation)
            if (animComp == null || stateComp == null || spriteComp == null) {
                continue;
            }

            // 2. Tăng thời gian tích lũy cho trạng thái hiện tại
            stateComp.timeInState += deltaTime;

            // 3. Lấy Animation tương ứng với State hiện tại (Ví dụ: IDLE, ATTACK)
            Animation<TextureRegion> animation = animComp.getAnimation(stateComp.get());

            if (animation != null) {
                // 4. Tính toán xem tại thời điểm này nên hiện Frame nào
                // (Nó tự xử lý việc lặp lại dựa trên PlayMode đã set lúc tạo Animation)
                TextureRegion currentFrame = animation.getKeyFrame(stateComp.timeInState);

                // 5. [QUAN TRỌNG] Cập nhật hình ảnh mới vào Sprite
                // (Hàm này bạn vừa thêm vào SpriteComponent ở bước trước)
                spriteComp.setRegion(currentFrame);
            }
        }
    }
}