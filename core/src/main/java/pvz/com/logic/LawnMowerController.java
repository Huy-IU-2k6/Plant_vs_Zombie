package pvz.com.logic;

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

    public LawnMowerController(float worldWidth, float mowerStartX) {
        this.worldWidth = worldWidth;
        this.mowerStartX = mowerStartX;
    }

    public void createLawnMowers() {
        lawnMowers.clear();

        for (int row = 0; row < laneCount; row++) {
            // Grid center Y của row hiện tại
            float laneCenterY = GridConfig.getCellCenterY(row);
            // offset xuống 1 chút cho phù hợp sprite (giống logic cũ: -50f)
            float mowerY = laneCenterY;

            lawnMowers.add(new LawnMower(mowerStartX, mowerY, worldWidth));
        }
    }

    /**
     * zombies: list zombie hiện tại trong game (base class Zombies)
     */
    public void update(float delta, Array<Zombies> zombies) {
        for (int i = lawnMowers.size - 1; i >= 0; i--) {
            LawnMower mower = lawnMowers.get(i);
            mower.update(delta, zombies);

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

    public void dispose() {
        for (LawnMower mower : lawnMowers) {
            if (!mower.isUsed()) {
                mower.dispose();
            }
        }
        lawnMowers.clear();
    }
}
