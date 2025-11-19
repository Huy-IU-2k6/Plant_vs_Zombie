package pvz.com.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class GameScreen implements Screen {

    private Game game;

    // 1. Khai báo các biến cho game
    private SpriteBatch batch;
    private Texture backgroundTexture; // Ảnh nền sân cỏ
    private Stage hudStage; // Stage riêng cho HUD (thẻ bài, mặt trời)

    public GameScreen(Game game) {
        this.game = game;

        // Khởi tạo Batch để vẽ ảnh
        batch = new SpriteBatch();

        // Khởi tạo Stage cho UI (HUD)
        hudStage = new Stage(new ScreenViewport());

        // Tạm thời tải ảnh background (đảm bảo bạn có file này hoặc file khác thay thế)
        // Nếu chưa có, hãy comment dòng dưới lại để tránh lỗi
        // backgroundTexture = new Texture("lawn_background.png");
    }

    @Override
    public void show() {
        // Khi màn hình này hiện ra, ta cho phép người dùng tương tác với HUD
        Gdx.input.setInputProcessor(hudStage);
    }

    @Override
    public void render(float delta) {
        // 1. Xóa màn hình (Màu xanh lá cây nhạt để giả làm sân cỏ nếu chưa có ảnh)
        Gdx.gl.glClearColor(0, 0.5f, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // 2. Vẽ thế giới game (Background, Plants, Zombies)
        batch.begin();
        if (backgroundTexture != null) {
            batch.draw(backgroundTexture, 0, 0);
        }
        batch.end();

        // 3. Vẽ HUD (UI) lên trên cùng
        hudStage.act(delta);
        hudStage.draw();
    }

    @Override
    public void resize(int width, int height) {
        hudStage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        batch.dispose();
        hudStage.dispose();
        if (backgroundTexture != null)
            backgroundTexture.dispose();
    }

    // Các hàm chưa dùng tới
    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }
}
