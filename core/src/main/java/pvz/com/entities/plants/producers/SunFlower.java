package pvz.com.entities.plants.producers;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import pvz.com.entities.plants.Plant;
import pvz.com.entities.components.*;
import pvz.com.managers.DesignConfig;
import pvz.com.managers.GridConfig;
import pvz.com.factories.PlantAssetLoader;

public class SunFlower extends Plant {

    private static final float SCALE_X = 0.7f;
    private static final float SCALE_Y = 0.8f;

    public SunFlower(float x, float y, int col, int row) {
        super(
                x,
                y,
                GridConfig.CELL_WIDTH * SCALE_X,
                GridConfig.CELL_HEIGHT * SCALE_Y);

        // 1. Lấy Animation từ Loader (Đã load ở GameScreen)
        var idleAnim = PlantAssetLoader.SUNFLOWER_IDLE;

        // Kiểm tra null để tránh crash nếu quên load
        if (idleAnim == null) {
            System.err.println("CRITICAL ERROR: PlantAssetLoader.SUNFLOWER_IDLE is NULL. Did you call loadAll()?");
            return;
        }

        // [QUAN TRỌNG - FIX LỖI CASTING]
        // Dùng getKeyFrame(0) để lấy 1 ảnh duy nhất. 
        // TUYỆT ĐỐI KHÔNG DÙNG getKeyFrames() (số nhiều) ở đây vì sẽ gây lỗi ClassCastException.
        TextureRegion firstFrame = idleAnim.getKeyFrame(0);

        // 2. Thiết lập Components
        this.addComponent(new SpriteComponent(firstFrame));

        AnimationComponent animComp = new AnimationComponent();
        animComp.addAnimation(EntityState.IDLE, idleAnim);
        this.addComponent(animComp);

        this.addComponent(new StateComponent(EntityState.IDLE));
        this.addComponent(new HealthComponent(100));
        this.addComponent(new TeamComponent(Team.PLANT));
        this.addComponent(new GridCellComponent(col, row));
        this.addComponent(new SunProducerComponent(DesignConfig.SUN_COOL_DOWN, 25));
    }

    public SunFlower(float x, float y) {
        this(x, y, -1, -1);
    }
}