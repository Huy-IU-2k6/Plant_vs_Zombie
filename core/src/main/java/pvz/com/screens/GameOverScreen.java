package pvz.com.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;

public class GameOverScreen extends ScreenAdapter {

    private final Game game;
    private final GameScreen gameScreen;
    private final boolean playerWon;

    private SpriteBatch batch;
    private BitmapFont font;
    private GlyphLayout layout;

    private Texture pixel; // 1x1 white pixel để vẽ overlay
    private FrameBuffer fbo; // chụp lại frame cuối
    private Texture snapshot; // texture từ FBO (lưu ý bị lật Y)

    // độ mờ overlay
    private static final float OVERLAY_ALPHA = 0.55f;

    public GameOverScreen(Game game, GameScreen gameScreen, boolean playerWon) {
        this.game = game;
        this.gameScreen = gameScreen;
        this.playerWon = playerWon;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        font = new BitmapFont();
        layout = new GlyphLayout();

        // pixel 1x1
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(Color.WHITE);
        pm.fill();
        pixel = new Texture(pm);
        pm.dispose();

        captureSnapshot();
    }

    private void captureSnapshot() {
        int w = Gdx.graphics.getBackBufferWidth();
        int h = Gdx.graphics.getBackBufferHeight();

        if (fbo != null)
            fbo.dispose();
        fbo = new FrameBuffer(Pixmap.Format.RGBA8888, w, h, false);

        fbo.begin();
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Vẽ lại “frame cuối” của GameScreen, KHÔNG update logic
        gameScreen.renderFrozen();

        fbo.end();

        snapshot = fbo.getColorBufferTexture();
        snapshot.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
    }

    @Override
    public void render(float delta) {
        // input cơ bản (tùy thích): ENTER/SPACE restart
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            game.setScreen(new GameScreen(game));
            return;
        }

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        int w = Gdx.graphics.getWidth();
        int h = Gdx.graphics.getHeight();

        batch.begin();

        // 1) Vẽ snapshot (FBO texture bị flip Y => v = 1 -> 0)
        batch.setColor(Color.WHITE);
        batch.draw(snapshot, 0, 0, w, h, 0f, 1f, 1f, 0f);

        // 2) Overlay mờ
        batch.setColor(0f, 0f, 0f, OVERLAY_ALPHA);
        batch.draw(pixel, 0, 0, w, h);
        batch.setColor(Color.WHITE);

        // 3) Text giữa màn
        String title = playerWon ? "YOU WIN!" : "GAME OVER";
        font.getData().setScale(3.0f);
        layout.setText(font, title);

        float tx = (w - layout.width) / 2f;
        float ty = (h + layout.height) / 2f;

        font.setColor(1f, 1f, 1f, 1f);
        font.draw(batch, layout, tx, ty);

        // gợi ý nhỏ
        font.getData().setScale(1.2f);
        String hint = "Press ENTER to restart";
        layout.setText(font, hint);
        font.setColor(1f, 1f, 1f, 0.85f);
        font.draw(batch, layout, (w - layout.width) / 2f, ty - 90f);

        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        // nếu resize cửa sổ thì chụp lại snapshot cho đúng tỉ lệ
        if (batch != null)
            captureSnapshot();
    }

    @Override
    public void dispose() {
        if (batch != null)
            batch.dispose();
        if (font != null)
            font.dispose();
        if (pixel != null)
            pixel.dispose();
        if (fbo != null)
            fbo.dispose(); // snapshot nằm trong fbo
    }
}
