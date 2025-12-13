package pvz.com.entities.plants.bombs;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

import pvz.com.entities.plants.Plant;
import pvz.com.entities.components.*;
import pvz.com.managers.GridConfig;

public class CherryBomb extends Plant {

    public CherryBomb(float x, float y, int col, int row) {
        // Kích thước 90x90
        super(x, y, 90, 90);

        // =============================================================
        // 1. TẠO ANIMATION
        // =============================================================
        
        // A. IDLE (Ngòi nổ cháy): CherryBomb_0.png -> CherryBomb_6.png
        Array<TextureRegion> idleFrames = new Array<>();
        for (int i = 0; i <= 6; i++) {
            idleFrames.add(new TextureRegion(new Texture("images/Plants/CherryBomb/CherryBomb/CherryBomb_" + i + ".png")));
        }
        Animation<TextureRegion> idleAnim = new Animation<>(0.15f, idleFrames, Animation.PlayMode.LOOP);

        // B. EXPLODE (Nổ Bùm): Boom_0.png -> Boom_4.png
        Array<TextureRegion> explodeFrames = new Array<>();
        for (int i = 18; i <= 28; i++) {
            explodeFrames.add(new TextureRegion(new Texture("images/Plants/CherryBomb/powie/powie_" + i + ".png")));
        }
        // Quan trọng: Animation nổ chỉ chạy 1 lần (NORMAL), không lặp
        Animation<TextureRegion> explodeAnim = new Animation<>(0.1f, explodeFrames, Animation.PlayMode.NORMAL);

        // =============================================================
        // 2. COMPONENTS
        // =============================================================

        this.addComponent(new SpriteComponent(idleFrames.first()));

        AnimationComponent animComp = new AnimationComponent();
        animComp.addAnimation(EntityState.IDLE, idleAnim);
        animComp.addAnimation(EntityState.EXPLODING, explodeAnim);
        this.addComponent(animComp);

        this.addComponent(new StateComponent(EntityState.IDLE));
        this.addComponent(new HealthComponent(300));
        this.addComponent(new TeamComponent(Team.PLANT));
        this.addComponent(new GridCellComponent(col, row));

        // [CƠ CHẾ NỔ]
        // Range 150f: Bán kính nổ.
        // Ô lưới khoảng 80x90. 150f tính từ tâm sẽ quét được 3x3 ô xung quanh.
        this.addComponent(new ExplosiveComponent(1800, 250f, 1.0f)); 
    }
}