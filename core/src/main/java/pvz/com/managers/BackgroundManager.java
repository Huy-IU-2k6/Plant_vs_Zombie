package pvz.com.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;

public class BackgroundManager {

    private Texture countBg;
    private Texture menuBg;
    private Texture mainBg;
    private Texture subBg;

    public BackgroundManager() {
        countBg = loadTexture("images/backgrounds/count_bg.jpeg");
        menuBg = loadTexture("images/backgrounds/menu_bg.png");
        mainBg = loadTexture("images/backgrounds/main_bg.png");
        subBg = loadTexture("images/backgrounds/sub_bg.png");

        countBg.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        menuBg.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        mainBg.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        subBg.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
    }

    private Texture loadTexture(String path) {
        try {
            Texture tex = new Texture(Gdx.files.internal(path));
            tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            return tex;
        } catch (Exception e) {
            Gdx.app.error("BackgroundManager", "Không load được background: " + path, e);
            return null;
        }
    }

    public void renderCount(Batch batch, float width, float height) {
        if (mainBg != null) {
            batch.draw(countBg, 0, 0, width, height);
        }
    }

    public void renderMenu(Batch batch, float width, float height) {
        if (menuBg != null) {
            batch.draw(menuBg, 0, 0, width, height);
        }
    }

    public void renderMain(Batch batch, float width, float height) {
        if (mainBg != null) {
            batch.draw(mainBg, 0, 0, width, height);
        }
    }

    public void renderSub(Batch batch, float width, float height) {
        if (subBg != null) {
            batch.draw(subBg, 0, 0, width, height);
        }
    }

    public void dispose() {
        disposeTexture(menuBg);
        disposeTexture(mainBg);
        disposeTexture(subBg);

        menuBg = null;
        mainBg = null;
        subBg = null;
    }

    private void disposeTexture(Texture tex) {
        if (tex != null) {
            tex.dispose();
        }
    }
}
