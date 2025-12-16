package pvz.com.entities.plants.bombs;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

import pvz.com.entities.plants.Plant;
import pvz.com.entities.components.*;
import pvz.com.managers.GridConfig;

public class PotatoMine extends Plant {

    // Kích thước hiển thị (Scale nhỏ lại chút cho vừa ô)
    private static final float SCALE = 0.8f;
    
    // Thời gian animation
    private static final float FRAME_DURATION = 0.15f; 

    public PotatoMine(float x, float y, int col, int row) {
        super(x, y, GridConfig.CELL_WIDTH * SCALE, GridConfig.CELL_HEIGHT * SCALE);

        // =============================================================
        // 1. LOAD 3 BỘ ANIMATION
        // =============================================================
        AnimationComponent animComp = new AnimationComponent();

        // A. TRẠNG THÁI 1: UNARMED (Cục đất / Init)
        // Load ảnh từ: images/Plants/PotatoMine/Init/PotatoMineInit_0.png...
        Animation<TextureRegion> initAnim = loadAnimation("images/Plants/PotatoMine/Init/PotatoMineInit_", 1, Animation.PlayMode.LOOP);
        animComp.addAnimation(EntityState.UNARMED, initAnim);

        // B. TRẠNG THÁI 2: IDLE (Đã mọc lên / Grow)
        // Load ảnh từ: images/Plants/PotatoMine/Idle/PotatoMine_0.png...
        Animation<TextureRegion> idleAnim = loadAnimation("images/Plants/PotatoMine/Idle/PotatoMine_", 8, Animation.PlayMode.LOOP);
        animComp.addAnimation(EntityState.IDLE, idleAnim);

        // C. TRẠNG THÁI 3: EXPLODING (Nổ / Boom)
        // Load ảnh từ: images/Plants/PotatoMine/Boom/PotatoMineBoom_0.png...
        // Đây là ảnh hiệu ứng nổ "Spudow!"
        Animation<TextureRegion> boomAnim = loadAnimation("images/Plants/PotatoMine/Boom/PotatoMineBoom_", 1, Animation.PlayMode.NORMAL);
        animComp.addAnimation(EntityState.EXPLODING, boomAnim);
        
        this.addComponent(animComp);

        // =============================================================
        // 2. KHỞI TẠO TRẠNG THÁI BAN ĐẦU
        // =============================================================
        
        // Bắt đầu là cục đất (UNARMED) và dùng frame đầu của initAnim làm hình đại diện
        this.addComponent(new StateComponent(EntityState.UNARMED));
        this.addComponent(new SpriteComponent(initAnim.getKeyFrame(0)));

        // =============================================================
        // 3. CÁC COMPONENT LOGIC
        // =============================================================

        // Máu 300 (Zombies ăn được lúc chưa nổ)
        this.addComponent(new HealthComponent(300));

        // Component quản lý thời gian trồi lên (3 giây test, game thật 14s)
        this.addComponent(new ArmingComponent(3.0f));

        this.addComponent(new TeamComponent(Team.PLANT));
        this.addComponent(new GridCellComponent(col, row));

        // Dữ liệu nổ: Damage 1800, Phạm vi nhỏ (50f ~ nửa ô), thời gian kích nổ 0s (đạp là nổ)
        this.addComponent(new ExplosiveComponent(1800, 50f, 0f));
    }

    // Hàm tiện ích để load animation cho gọn code
    private Animation<TextureRegion> loadAnimation(String prefix, int frameCount, Animation.PlayMode mode) {
        Array<TextureRegion> frames = new Array<>();
        for (int i = 0; i < frameCount; i++) {
            try {
                // Texture tex = new Texture(prefix + i + ".png"); // Nếu bạn có nhiều ảnh
                
                // [LƯU Ý] Nếu bạn chỉ có 1 ảnh duy nhất cho mỗi trạng thái (chưa cắt sprite sheet)
                // thì sửa logic chỗ này. Ở đây mình giả sử bạn có file _0.png, _1.png...
                Texture tex = new Texture(prefix + i + ".png");
                tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
                frames.add(new TextureRegion(tex));
            } catch (Exception e) {
                // Bỏ qua nếu thiếu ảnh
            }
        }
        // Fallback nếu không load được ảnh nào (tránh crash)
        if (frames.size == 0) {
             frames.add(new TextureRegion(new Texture("images/Plants/PotatoMine.png"))); 
        }
        
        return new Animation<>(FRAME_DURATION, frames, mode);
    }
}