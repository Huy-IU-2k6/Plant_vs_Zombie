package pvz.com.managers;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

/**
 * Class tiện ích chuyên xử lý spritesheet → Animation.
 * Dùng được cho cả gif/png miễn là file đó là spritesheet.
 */
public final class GifManager {

    private GifManager() {
        // không cho new
    }

    /**
     * Tạo Animation từ 1 spritesheet 1 hàng gồm nhiều frame.
     *
     * @param sheet         Texture của spritesheet
     * @param frameCount    số frame trên 1 hàng
     * @param frameDuration thời gian 1 frame (giây)
     * @param playMode      chế độ play (LOOP, NORMAL, REVERSED, …)
     */
    public static Animation<TextureRegion> createAnim(Texture sheet,
            int frameCount,
            float frameDuration,
            Animation.PlayMode playMode) {

        int frameWidth = sheet.getWidth() / frameCount;
        int frameHeight = sheet.getHeight();

        TextureRegion[][] tmp = TextureRegion.split(sheet, frameWidth, frameHeight);

        Array<TextureRegion> frames = new Array<>(frameCount);
        for (int i = 0; i < frameCount; i++) {
            frames.add(tmp[0][i]);
        }

        Animation<TextureRegion> anim = new Animation<>(frameDuration, frames);
        anim.setPlayMode(playMode);
        return anim;
    }
}
