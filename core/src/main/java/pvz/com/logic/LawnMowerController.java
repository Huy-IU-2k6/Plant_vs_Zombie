package pvz.com.logic;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;

import pvz.com.entities.Zombies.Zombies;
import pvz.com.items.LawnMower;
import pvz.com.managers.GridConfig;
import pvz.com.managers.DesignConfig;

public class LawnMowerController {

    private final Array<LawnMower> lawnMowers = new Array<>();
    private final int laneCount = DesignConfig.ROWS;
    private final float worldWidth;
    private final float mowerStartX;

    // [TỐI ƯU] Load Texture 1 lần ở đây để dùng chung
    private final Texture idleTexture;
    private final Texture activeTexture;

    public LawnMowerController(float worldWidth, float mowerStartX) {
        this.worldWidth = worldWidth;
        this.mowerStartX = mowerStartX;

        // Load ảnh (Nên dùng .png thay vì .gif)
        // Lưu ý: Texture mặc định không chạy animation gif
        this.idleTexture = new Texture(Gdx.files.internal("images/items/lawnMower_Idle.png"));
        this.activeTexture = new Texture(Gdx.files.internal("images/items/lawnMower_Idle.png")); // Đổi đuôi sang png
        
        // Bật lọc ảnh cho mượt
        this.idleTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        this.activeTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
    }

    public void createLawnMowers() {
        disposeMowers(); // Xóa cũ nếu có
        lawnMowers.clear();

        for (int row = 0; row < laneCount; row++) {
            float laneCenterY = GridConfig.getCellCenterY(row);
            // Canh chỉnh lại Y cho chuẩn tâm
            float mowerY = laneCenterY - (DesignConfig.FIXED_WIDTH / 2f); 

            // Truyền Texture vào constructor
            lawnMowers.add(new LawnMower(mowerStartX, mowerY, worldWidth, idleTexture, activeTexture));
        }
    }

    public void update(float delta, Array<Zombies> zombies) {
        for (int i = lawnMowers.size - 1; i >= 0; i--) {
            LawnMower mower = lawnMowers.get(i);
            mower.update(delta, zombies);

            // Chỉ xóa khỏi list quản lý, không dispose texture (vì texture dùng chung)
            if (mower.isUsed()) {
                lawnMowers.removeIndex(i);
            }
        }
    }

    public void render(SpriteBatch batch) {
        for (LawnMower mower : lawnMowers) {
            mower.render(batch);
        }
    }

    private void disposeMowers() {
        // Mower không giữ tài nguyên nặng nữa nên không cần gọi dispose từng cái
        lawnMowers.clear();
    }

    public void dispose() {
        disposeMowers();
        // [QUAN TRỌNG] Dispose texture gốc ở đây
        if (idleTexture != null) idleTexture.dispose();
        if (activeTexture != null) activeTexture.dispose();
    }
}