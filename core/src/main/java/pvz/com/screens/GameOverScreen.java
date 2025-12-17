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
import com.badlogic.gdx.utils.BufferUtils;

import java.nio.ByteBuffer;

public class GameOverScreen extends ScreenAdapter {

    private final Game game;
    private final GameScreen gameScreen;
    private final boolean playerWon;

    private SpriteBatch batch;
    private BitmapFont font;
    private GlyphLayout layout;

    private Texture pixel; // 1x1 white pixel
    private Texture snapshotTex; // ✅ snapshot đúng chiều

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

        FrameBuffer fbo = new FrameBuffer(Pixmap.Format.RGBA8888, w, h, false);

        fbo.begin();
        Gdx.gl.glViewport(0, 0, w, h);
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // vẽ lại frame cuối, không update logic
        gameScreen.renderFrozen();

        // ✅ đọc pixels (không deprecated)
        Pixmap shot = readPixelsToPixmapFlippedY(0, 0, w, h);

        fbo.end();

        // trả viewport về màn hình
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());

        // cập nhật texture snapshot
        if (snapshotTex != null)
            snapshotTex.dispose();
        snapshotTex = new Texture(shot);
        snapshotTex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        shot.dispose();
        fbo.dispose();
    }

    // glReadPixels cho dữ liệu gốc bottom-left -> cần flip Y để ra đúng chiều khi
    // draw
    private static Pixmap readPixelsToPixmapFlippedY(int x, int y, int w, int h) {
        ByteBuffer pixels = BufferUtils.newByteBuffer(w * h * 4);
        Gdx.gl.glReadPixels(x, y, w, h, GL20.GL_RGBA, GL20.GL_UNSIGNED_BYTE, pixels);

        Pixmap pm = new Pixmap(w, h, Pixmap.Format.RGBA8888);

        int rowBytes = w * 4;
        byte[] row = new byte[rowBytes];

        for (int srcRow = 0; srcRow < h; srcRow++) {
            int srcPos = srcRow * rowBytes;
            pixels.position(srcPos);
            pixels.get(row);

            int dstRow = h - 1 - srcRow; // flip Y
            pm.getPixels().position(dstRow * rowBytes);
            pm.getPixels().put(row);
        }

        pm.getPixels().position(0);
        return pm;
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            game.setScreen(new GameScreen(game));
            return;
        }

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        int w = Gdx.graphics.getWidth();
        int h = Gdx.graphics.getHeight();

        batch.begin();

        // 1) background snapshot (đúng chiều)
        batch.setColor(Color.WHITE);
        if (snapshotTex != null)
            batch.draw(snapshotTex, 0, 0, w, h);

        // 2) overlay
        batch.setColor(0f, 0f, 0f, OVERLAY_ALPHA);
        batch.draw(pixel, 0, 0, w, h);
        batch.setColor(Color.WHITE);

        // 3) text
        String title = playerWon ? "YOU WIN!" : "GAME OVER";
        font.getData().setScale(3.0f);
        layout.setText(font, title);

        float tx = (w - layout.width) / 2f;
        float ty = (h + layout.height) / 2f;

        font.setColor(1f, 1f, 1f, 1f);
        font.draw(batch, layout, tx, ty);

        font.getData().setScale(1.2f);
        String hint = "Press ENTER to restart";
        layout.setText(font, hint);
        font.setColor(1f, 1f, 1f, 0.85f);
        font.draw(batch, layout, (w - layout.width) / 2f, ty - 90f);

        batch.end();
    }

    @Override
    public void resize(int width, int height) {
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
        if (snapshotTex != null)
            snapshotTex.dispose();
    }
}
