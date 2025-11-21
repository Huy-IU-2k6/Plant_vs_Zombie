package pvz.com.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

public class FontManager {

    private static BitmapFont pvzFont;

    private FontManager() {
        // chặn new FontManager()
    }

    public static BitmapFont getPvzFont() {
        if (pvzFont == null) {
            createPvzFont();
        }
        return pvzFont;
    }

    private static void createPvzFont() {
        FreeTypeFontGenerator gen = new FreeTypeFontGenerator(
                // chỉnh lại path cho đúng với project của bạn
                Gdx.files.internal("assets/fonts/HouseofTerror/HouseofTerror Regular.ttf")
        );
        FreeTypeFontGenerator.FreeTypeFontParameter param =
                new FreeTypeFontGenerator.FreeTypeFontParameter();

        param.size = 50;

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

    public static void dispose() {
        if (pvzFont != null) {
            pvzFont.dispose();
            pvzFont = null;
        }
    }
}

