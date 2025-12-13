package pvz.com.entities.plants.shooters;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

import pvz.com.entities.plants.Plant;
import pvz.com.entities.components.*;
import pvz.com.entities.projectiles.PeaProjectile;
import pvz.com.managers.GridConfig;

public class Repeater extends Plant {
    private static final int FRAME_COUNT = 15; // Giả sử dùng chung số frame với Peashooter
    private static final float FRAME_DURATION = 0.12f;

    public Repeater(float x, float y, int col, int row) {
        super(x, y, GridConfig.CELL_WIDTH, GridConfig.CELL_HEIGHT);

        // 1. Load Animation (Bạn cần bộ ảnh Repeater riêng, nó có lông mày ngầu hơn)
        // Tạm thời dùng ảnh Peashooter nếu chưa có
        Array<TextureRegion> frames = new Array<>();
        for (int i = 0; i < FRAME_COUNT; i++) {
            // Nhớ tạo folder: images/Plants/Repeater/
            Texture tex = new Texture("images/Plants/RepeaterPea/RepeaterPea_" + i + ".png");
            tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            frames.add(new TextureRegion(tex));
        }
        Animation<TextureRegion> anim = new Animation<>(FRAME_DURATION, frames, Animation.PlayMode.LOOP);

        this.addComponent(new SpriteComponent(frames.first()));
        
        AnimationComponent animComp = new AnimationComponent();
        animComp.addAnimation(EntityState.IDLE, anim);
        animComp.addAnimation(EntityState.ATTACKING, anim);
        this.addComponent(animComp);

        this.addComponent(new StateComponent(EntityState.IDLE));
        this.addComponent(new HealthComponent(100)); // Máu bằng Peashooter
        this.addComponent(new TeamComponent(Team.PLANT));
        this.addComponent(new GridCellComponent(col, row));

        // 2. CẤU HÌNH TẤN CÔNG (QUAN TRỌNG)
        PlantAttackComponent attackComp = new PlantAttackComponent(
                20,                  // Damage mỗi viên
                900f,                // Tầm bắn
                PeaProjectile.class, // Loại đạn
                PlantDamageType.NORMAL,
                1.5f                 // Tốc độ hồi chiêu giữa các LOẠT bắn (1.5s bắn 1 loạt)
        );
        
        // [KEY] Kích hoạt chế độ bắn 2 viên
        // 2 viên mỗi loạt, cách nhau 0.15 giây
        attackComp.setBurstFire(2, 0.15f); 
        
        this.addComponent(attackComp);
    }
}