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
    // [FIX 1] Thêm tỷ lệ Scale (0.7f nghĩa là to bằng 70% ô đất)
    // Bạn có thể giảm xuống 0.6f nếu vẫn thấy to
    private static final float SCALE_X = 0.7f;
    private static final float SCALE_Y = 0.75f;

    private static final int FRAME_COUNT = 15;
    private static final float FRAME_DURATION = 0.12f;

    public Repeater(float x, float y, int col, int row) {
        // [FIX 2] Nhân kích thước ô với tỷ lệ Scale để cây nhỏ lại
        super(x, y, GridConfig.CELL_WIDTH * SCALE_X, GridConfig.CELL_HEIGHT * SCALE_Y);

        // 1. Load Animation
        Array<TextureRegion> frames = new Array<>();
        for (int i = 0; i < FRAME_COUNT; i++) {
            Texture tex = new Texture("images/Plants/RepeaterPea/RepeaterPea_" + i + ".png");
            tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            frames.add(new TextureRegion(tex));
        }
        Animation<TextureRegion> anim = new Animation<>(FRAME_DURATION, frames, Animation.PlayMode.LOOP);

        this.addComponent(new SpriteComponent(frames.first()));

        AnimationComponent animComp = new AnimationComponent();
        animComp.addAnimation(EntityState.IDLE, anim);

        // [LƯU Ý] Kiểm tra kỹ xem hệ thống của bạn dùng "ATTACK" hay "ATTACKING"
        // Trong PlantAttackSystem hôm trước mình gửi thì dùng EntityState.ATTACK
        animComp.addAnimation(EntityState.ATTACKING, anim);
        this.addComponent(animComp);

        this.addComponent(new StateComponent(EntityState.IDLE));
        this.addComponent(new HealthComponent(100));
        this.addComponent(new TeamComponent(Team.PLANT));
        this.addComponent(new GridCellComponent(col, row));

        // 2. CẤU HÌNH TẤN CÔNG
        PlantAttackComponent attackComp = new PlantAttackComponent(
                20,
                900f,
                PeaProjectile.class,
                PlantDamageType.NORMAL,
                1.5f);

        // Bắn 2 viên
        attackComp.setBurstFire(2, 0.15f);

        this.addComponent(attackComp);
    }
}
