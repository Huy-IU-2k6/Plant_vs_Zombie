package pvz.com.entities.plants.bombs;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

import pvz.com.entities.plants.Plant;
import pvz.com.entities.components.*;
import pvz.com.managers.GridConfig;

public class PotatoMine extends Plant {

    // [CẤU HÌNH KÍCH THƯỚC]
    // Kích thước chuẩn (Lớn) dùng cho lúc đã mọc và nổ (80% ô đất)
    public static final float BIG_WIDTH = GridConfig.CELL_WIDTH * 0.8f;
    public static final float BIG_HEIGHT = GridConfig.CELL_HEIGHT * 0.8f;

    // Kích thước lúc còn là cái núm (Nhỏ) (40% ô đất)
    public static final float SMALL_WIDTH = GridConfig.CELL_WIDTH * 0.4f;
    public static final float SMALL_HEIGHT = GridConfig.CELL_HEIGHT * 0.4f;

    private static final float FRAME_DURATION = 0.06f; 

    public PotatoMine(float x, float y, int col, int row) {
        // 1. [QUAN TRỌNG] Khởi tạo với kích thước NHỎ (SMALL)
        super(x, y, SMALL_WIDTH, SMALL_HEIGHT);

        // 2. [CĂN GIỮA] Vì nó nhỏ đi, ta phải tính toán lại vị trí để nó nằm giữa ô
        float centerX = GridConfig.getCellCenterX(col);
        float centerY = GridConfig.getCellCenterY(row);

        // Cập nhật lại PositionComponent (ghi đè lên vị trí của super)
        PositionComponent pos = this.getComponent(PositionComponent.class);
        if (pos != null) {
            pos.x = centerX - (SMALL_WIDTH / 2f);
            pos.y = centerY - (SMALL_HEIGHT / 2f);
        }

        // --- LOAD ANIMATION ---
        AnimationComponent animComp = new AnimationComponent();

        Animation<TextureRegion> growAnim = loadAnimation("images/Plants/PotatoMine/planted/planted_", 29, Animation.PlayMode.NORMAL);
        animComp.addAnimation(EntityState.GROWING, growAnim);

        Animation<TextureRegion> initAnim = loadAnimation("images/Plants/PotatoMine/init/init_", 1, Animation.PlayMode.LOOP);
        animComp.addAnimation(EntityState.UNARMED, initAnim);

        Animation<TextureRegion> riseAnim = loadAnimation("images/Plants/PotatoMine/grow/grow_", 25, Animation.PlayMode.NORMAL);
        animComp.addAnimation(EntityState.RISING, riseAnim);

        Animation<TextureRegion> idleAnim = loadAnimation("images/Plants/PotatoMine/Idle/idle_", 30, Animation.PlayMode.LOOP);
        animComp.addAnimation(EntityState.IDLE, idleAnim);

        Animation<TextureRegion> boomAnim = loadAnimation("images/Plants/PotatoMine/explode/explode_", 26, Animation.PlayMode.NORMAL);
        animComp.addAnimation(EntityState.EXPLODING, boomAnim);
        
        this.addComponent(animComp);

        // KHỞI TẠO TRẠNG THÁI
        this.addComponent(new StateComponent(EntityState.GROWING));
        this.addComponent(new SpriteComponent(growAnim.getKeyFrame(0)));

        this.addComponent(new HealthComponent(300));
        this.addComponent(new ArmingComponent(3.0f)); 
        this.addComponent(new TeamComponent(Team.PLANT));
        this.addComponent(new GridCellComponent(col, row));
        
        // Range nổ vẫn giữ nguyên
        this.addComponent(new ExplosiveComponent(1800, 150f, -1f));
    }

    private Animation<TextureRegion> loadAnimation(String prefix, int count, Animation.PlayMode mode) {
        Array<TextureRegion> frames = new Array<>();
        for (int i = 0; i < count; i++) {
             try {
                Texture tex = new Texture(prefix + i + ".png");
                tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
                frames.add(new TextureRegion(tex));
             } catch (Exception e) {}
        }
        if (frames.size == 0) return null;
        return new Animation<>(FRAME_DURATION, frames, mode);
    }
}