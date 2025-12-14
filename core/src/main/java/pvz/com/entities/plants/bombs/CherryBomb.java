package pvz.com.entities.plants.bombs;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

import pvz.com.entities.plants.Plant;
import pvz.com.entities.components.*;

public class CherryBomb extends Plant {
    private static final float FRAME_DURATION = 0.12f;

    // Nếu muốn nổ nhanh hơn idle thì dùng multiplier (tuỳ chọn)
    private static final float EXPLODE_FRAME_DURATION = FRAME_DURATION * 0.8f;

    public CherryBomb(float x, float y, int col, int row) {
        // Kích thước 90x90
        super(x, y, 90, 90);

        // =============================================================
        // 1. TẠO ANIMATION
        // =============================================================

        // A. IDLE (Ngòi nổ cháy): CherryBomb_0.png -> CherryBomb_6.png
        Array<TextureRegion> idleFrames = new Array<>();
        for (int i = 0; i <= 6; i++) {
            idleFrames.add(
                    new TextureRegion(new Texture("images/Plants/CherryBomb/CherryBomb/CherryBomb_" + i + ".png")));
        }
        Animation<TextureRegion> idleAnim = new Animation<>(FRAME_DURATION, idleFrames, Animation.PlayMode.LOOP);

        // B. EXPLODE (Nổ Bùm): powie_18.png -> powie_28.png
        Array<TextureRegion> explodeFrames = new Array<>();
        for (int i = 18; i <= 28; i++) {
            explodeFrames.add(
                    new TextureRegion(new Texture("images/Plants/CherryBomb/powie/powie_" + i + ".png")));
        }
        Animation<TextureRegion> explodeAnim = new Animation<>(EXPLODE_FRAME_DURATION, explodeFrames,
                Animation.PlayMode.NORMAL);

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
        this.addComponent(new ExplosiveComponent(1800, 250f, 1.0f));
    }
}
