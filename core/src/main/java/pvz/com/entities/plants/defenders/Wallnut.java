package pvz.com.entities.plants.defenders;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import pvz.com.entities.plants.Plant;
import pvz.com.entities.components.*;
import pvz.com.managers.GridConfig;

public class Wallnut extends Plant {

    private static final float SCALE_X = 0.7f;
    private static final float SCALE_Y = 0.8f;
    private static final float FRAME_DURATION = 0.15f; // Tốc độ animation
    private static final int TOTAL_HEALTH = 4000;

    public Wallnut(float x, float y, int gridCol, int gridRow) {
        super(
                x,
                y,
                GridConfig.CELL_WIDTH * SCALE_X,
                GridConfig.CELL_HEIGHT * SCALE_Y);

        // =================================================================
        // 1. LOAD ANIMATIONS CHO CẢ 3 TRẠNG THÁI
        // =================================================================
        
        // Helper function để load ảnh cho gọn (xem bên dưới)
        Animation<TextureRegion> animFull = loadAnimation("images/Plants/Wallnut/WallNut/WallNut_", 10); // Giả sử có 10 frame
        Animation<TextureRegion> animCrack1 = loadAnimation("images/Plants/Wallnut/WallNut_cracked1/Wallnut_cracked1_", 10);
        Animation<TextureRegion> animCrack2 = loadAnimation("images/Plants/Wallnut/WallNut_cracked2/Wallnut_cracked2_", 10);

        // =================================================================
        // 2. THIẾT LẬP CÁC COMPONENTS
        // =================================================================

        // A. SpriteComponent: Bắt đầu bằng frame đầu tiên của trạng thái FULL
        this.addComponent(new SpriteComponent(animFull.getKeyFrame(0)));

        // B. AnimationComponent: Chứa cả 3 bộ phim
        AnimationComponent animComp = new AnimationComponent();
        animComp.addAnimation(EntityState.WALLNUT_FULL, animFull);
        animComp.addAnimation(EntityState.WALLNUT_CRACKED_1, animCrack1);
        animComp.addAnimation(EntityState.WALLNUT_CRACKED_2, animCrack2);
        this.addComponent(animComp);

        // C. StateComponent: Trạng thái ban đầu là FULL
        this.addComponent(new StateComponent(EntityState.WALLNUT_FULL));

        // D. Các component khác
        this.addComponent(new HealthComponent(TOTAL_HEALTH));
        this.addComponent(new TeamComponent(Team.PLANT));
        this.addComponent(new GridCellComponent(gridCol, gridRow));
    }

    // Hàm tiện ích để load nhanh một thư mục ảnh
    private Animation<TextureRegion> loadAnimation(String prefixPath, int frameCount) {
        Array<TextureRegion> frames = new Array<>();
        for (int i = 0; i < frameCount; i++) {
            // Ví dụ path: "images/Plants/Wallnut/Full/Wallnut_0.png"
            Texture tex = new Texture(prefixPath + i + ".png");
            tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            frames.add(new TextureRegion(tex));
        }
        return new Animation<>(FRAME_DURATION, frames, Animation.PlayMode.LOOP);
    }

    public Wallnut(float x, float y) {
        this(x, y, -1, -1);
    }
}
