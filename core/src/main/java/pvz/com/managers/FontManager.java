package pvz.com.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

public class FontManager {

    // Size chuẩn ở base resolution (ví dụ 1920x1080)
    private static final int BASE_FONT_SIZE = 50;

    private static BitmapFont pvzFont;

    private FontManager() {
        // chặn new FontManager()
    }

    /** Font PVZ gốc, không scale theo màn hình */
    public static BitmapFont getPvzFont() {
        if (pvzFont == null) {
            createBasePvzFont();
        }
        return pvzFont;
    }

    private static void createBasePvzFont() {
        FreeTypeFontGenerator gen = new FreeTypeFontGenerator(
                Gdx.files.internal("fonts/HouseofTerror/HouseofTerror Regular.ttf"));

        FreeTypeFontGenerator.FreeTypeFontParameter param = new FreeTypeFontGenerator.FreeTypeFontParameter();

        param.size = BASE_FONT_SIZE;

        // Màu chữ kem
        param.color = new Color(0xFBE3B5FF);

        // Viền nâu đậm quanh chữ
        param.borderWidth = 3f;
        param.borderColor = new Color(0xA46B3AFF);
        param.borderStraight = true;

        // Bóng mờ nhẹ
        param.shadowOffsetX = 2;
        param.shadowOffsetY = -2;
        param.shadowColor = new Color(0f, 0f, 0f, 0.35f);

        pvzFont = gen.generateFont(param);
        gen.dispose();

        // Giảm răng cưa khi scale
        Texture tex = pvzFont.getRegion().getTexture();
        tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
    }

    /**
     * Tính scale cho HUD text dựa trên worldWidth/Height.
     * Mặc định dùng min(scaleX, scaleY) để không méo chữ.
     */
    public static float computeHudFontScale(float worldWidth, float worldHeight) {
        float scaleX = HudLayoutConfig.getScaleX(worldWidth);
        float scaleY = HudLayoutConfig.getScaleY(worldHeight);
        return Math.min(scaleX, scaleY);
    }

    /**
     * Helper nếu muốn apply trực tiếp scale vào font.
     * (ở CountdownActor mình vẫn làm thủ công để restore scale cũ)
     */
    public static void applyHudScale(BitmapFont font, float worldWidth, float worldHeight) {
        float scale = computeHudFontScale(worldWidth, worldHeight);
        font.getData().setScale(scale);
    }

    public static void dispose() {
        if (pvzFont != null) {
            pvzFont.dispose();
            pvzFont = null;
        }
    }
}
