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
import com.badlogic.gdx.utils.Array;
import pvz.com.Zombies.NormalZombie;

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

    private final Array<NormalZombie> zombies = new Array<>();

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

    private void spawnInitialZombies() {
        // Ví dụ: tạo 5 zombie, mỗi lane một con
        float startX = WORLD_WIDTH + 50f; // xuất hiện ngoài mép phải tí cho đẹp
        float baseY = 100f;
        float laneGap = 80f;

        for (int i = 0; i < 5; i++) {
            NormalZombie z = new NormalZombie();

            // Đặt vị trí theo hàng
            z.setPosition(startX, baseY + i * laneGap);

            zombies.add(z);
        }
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(hudStage);
    }

    @Override
    public void render(float delta) {
        // 1. cập nhật logic state
        updateState(delta);

        // 2. cập nhật logic zombie (chỉ khi đang chơi)
        updateGame(delta);

        // 3. clear màn hình
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        batch.setProjectionMatrix(camera.combined);

        // 4. vẽ background + zombie
        batch.begin();
        float w = viewport.getWorldWidth();
        float h = viewport.getWorldHeight();

        if (state == State.COUNTDOWN) {
            backgroundManager.renderCount(batch, w, h);
        } else { // PLAYING
            backgroundManager.renderMain(batch, w, h);

            // Vẽ zombie
            for (NormalZombie z : zombies) {
                z.draw(batch, 1f);
            }

            // TODO: vẽ plant, bullet...
        }

        batch.end();

        // 5. Vẽ HUD (countdown...)
        hudStage.act(delta);
        hudStage.draw();

        // ESC -> ResumeScreen
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new ResumeScreen(game, this));
        }
    }

    private void updateGame(float delta) {
        if (state != State.PLAYING)
            return;

        for (NormalZombie z : zombies) {
            z.act(delta); // dùng logic act() bạn đã viết trong NormalZombie
        }

        // Sau này có thể:
        // - remove zombie nếu ra khỏi màn
        // - check va chạm bullet, plant...
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

                // === Spawn zombie đợt đầu tiên ===
                spawnInitialZombies();
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

        for (NormalZombie z : zombies) {
            z.dispose();
        }
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
