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

    private Texture pixel;
    private Texture snapshotTex;

    private static final float OVERLAY_ALPHA = 0.55f;
    private boolean restarting = false;

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

    private void restartToNewGame() {
        if (restarting)
            return;
        restarting = true;

        GameScreen newScreen = new GameScreen(game);

        game.setScreen(newScreen);

        dispose();

        if (gameScreen != null) {
            gameScreen.dispose();
        }
    }

    private void captureSnapshot() {
        if (gameScreen == null)
            return;

        int w = Gdx.graphics.getBackBufferWidth();
        int h = Gdx.graphics.getBackBufferHeight();

        FrameBuffer fbo = new FrameBuffer(Pixmap.Format.RGBA8888, w, h, false);

        fbo.begin();
        Gdx.gl.glViewport(0, 0, w, h);
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        gameScreen.renderFrozen();

        Pixmap shot = readPixelsToPixmapFlippedY(0, 0, w, h);

        fbo.end();
        fbo.dispose();

        Gdx.gl.glViewport(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());

        if (snapshotTex != null)
            snapshotTex.dispose();
        snapshotTex = new Texture(shot);
        snapshotTex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        shot.dispose();
    }

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

            int dstRow = h - 1 - srcRow;
            pm.getPixels().position(dstRow * rowBytes);
            pm.getPixels().put(row);
        }

        pm.getPixels().position(0);
        return pm;
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)
                || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            restartToNewGame();
            return;
        }

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        int w = Gdx.graphics.getWidth();
        int h = Gdx.graphics.getHeight();

        batch.begin();

        batch.setColor(Color.WHITE);
        if (snapshotTex != null)
            batch.draw(snapshotTex, 0, 0, w, h);

        batch.setColor(0f, 0f, 0f, OVERLAY_ALPHA);
        batch.draw(pixel, 0, 0, w, h);
        batch.setColor(Color.WHITE);

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
        batch = null;
        font = null;
        pixel = null;
        snapshotTex = null;
    }
}
