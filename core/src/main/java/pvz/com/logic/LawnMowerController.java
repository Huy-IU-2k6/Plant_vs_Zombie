package pvz.com.logic;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;

import pvz.com.Zombies.NormalZombie;
import pvz.com.items.LawnMower;
import pvz.com.managers.GridConfig;

public class LawnMowerController {

    private final Array<LawnMower> lawnMowers = new Array<>();

    private final float worldWidth;
    private final int laneCount;

    // cấu hình vị trí mower
    private final float mowerStartX;
    private final float mowerYOffset;

    public LawnMowerController(float worldWidth, int laneCount) {
        this(worldWidth, laneCount, 180f, 50f);
    }

    public LawnMowerController(float worldWidth, int laneCount, float mowerStartX, float mowerYOffset) {
        this.worldWidth = worldWidth;
        this.laneCount = laneCount;
        this.mowerStartX = mowerStartX;
        this.mowerYOffset = mowerYOffset;
    }

    /** Tạo mower cho mỗi lane một lần, dùng GridConfig để canh Y. */
    public void createLawnMowers() {
        lawnMowers.clear();

        for (int row = 0; row < laneCount; row++) {
            float laneCenterY = GridConfig.getCellCenterY(row);
            float mowerY = laneCenterY - mowerYOffset;

            lawnMowers.add(new LawnMower(mowerStartX, mowerY, worldWidth));
        }
    }

    /** Update tất cả mower + xử lý khi dùng xong thì remove. */
    public void update(float delta, Array<NormalZombie> zombies) {
        for (int i = lawnMowers.size - 1; i >= 0; i--) {
            LawnMower mower = lawnMowers.get(i);
            mower.update(delta, zombies);

            if (mower.isUsed()) {
                lawnMowers.removeIndex(i);
            }
        }
    }

    /** Vẽ mower. */
    public void render(SpriteBatch batch) {
        for (LawnMower mower : lawnMowers) {
            mower.render(batch);
        }
    }

    /** Giải phóng resource. */
    public void dispose() {
        for (LawnMower mower : lawnMowers) {
            if (!mower.isUsed()) {
                mower.dispose();
            }
        }
    }

    /** Nếu sau này cần truy cập trực tiếp list mower. */
    public Array<LawnMower> getLawnMowers() {
        return lawnMowers;
    }
}
