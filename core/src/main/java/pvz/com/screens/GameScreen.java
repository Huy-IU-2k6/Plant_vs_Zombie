package pvz.com.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.graphics.OrthographicCamera;

import pvz.com.managers.FontManager;
import pvz.com.managers.BackgroundManager;

public class GameScreen implements Screen {

    private static final float WORLD_WIDTH = 800f;
    private static final float WORLD_HEIGHT = 600f;

    private final Game game;

    private final SpriteBatch batch;
    private final Stage hudStage;

    private final OrthographicCamera camera;
    private final Viewport viewport;

    private final BackgroundManager backgroundManager;

    private CountdownActor countdown;

    // ====== STATE CHO GAME ======
    private enum State {
        COUNTDOWN, // đang hiển thị count_bg + đếm ngược
        PLAYING // đã đếm xong, chuyển sang main bg
    }

    private State state = State.COUNTDOWN;
    private float countdownTime = 6f; // phải trùng với thời gian truyền vào CountdownActor

    public GameScreen(Game game) {
        this.game = game;

        batch = new SpriteBatch();
        hudStage = new Stage(new ScreenViewport());

        // Camera + viewport 800x600
        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        camera.position.set(WORLD_WIDTH / 2f, WORLD_HEIGHT / 2f, 0);
        camera.update();

        // Background manager (đã có renderCount, renderMain, renderSub...)
        backgroundManager = new BackgroundManager();

        // Countdown 3 giây
        countdown = new CountdownActor(countdownTime, FontManager.getPvzFont());
        countdown.setPosition(400f, 500f);
        hudStage.addActor(countdown);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(hudStage);
    }

    @Override
    public void render(float delta) {
        // cập nhật logic state
        updateState(delta);

        // Xoá màn hình
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        batch.setProjectionMatrix(camera.combined);

        // Vẽ background tuỳ theo state
        batch.begin();
        float w = viewport.getWorldWidth();
        float h = viewport.getWorldHeight();

        if (state == State.COUNTDOWN) {
            // 1. Ban đầu (trong thời gian đếm ngược): dùng count_bg
            backgroundManager.renderCount(batch, w, h);
        } else {
            // 3. Đếm ngược xong: chuyển sang main bg
            backgroundManager.renderMain(batch, w, h);
            // TODO: vẽ plant, zombie, bullet... ở đây
        }

        batch.end();

        // Vẽ HUD (countdown text)
        hudStage.act(delta);
        hudStage.draw();

        // ESC -> ResumeScreen
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new ResumeScreen(game, this));
        }
    }

    private void updateState(float delta) {
        if (state == State.COUNTDOWN) {
            countdownTime -= delta;
            if (countdownTime <= 0f) {
                // Chuyển sang PLAYING
                state = State.PLAYING;

                // Ẩn/huỷ actor countdown nếu không cần nữa
                if (countdown != null) {
                    countdown.remove();
                    countdown = null;
                }
            }
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        hudStage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        batch.dispose();
        hudStage.dispose();
        backgroundManager.dispose();
        // FontManager dùng chung thì dispose ở Game chính
    }

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
