package pvz.com.screens;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.graphics.OrthographicCamera;

public class GameScreen implements Screen {

    private static final float WORLD_WIDTH = 800f;
    private static final float WORLD_HEIGHT = 600f;

    private final Game game;

    private final SpriteBatch batch;
    private final Stage hudStage;

    private final OrthographicCamera camera;
    private final Viewport viewport;

    private Texture bgTex; // Ảnh nền sân cỏ

    public GameScreen(Game game) {
        this.game = game;

        batch = new SpriteBatch();
        hudStage = new Stage(new ScreenViewport());

        // Camera + viewport 800x600, scale vừa màn hình
        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        camera.position.set(WORLD_WIDTH / 2f, WORLD_HEIGHT / 2f, 0);
        camera.update();

        try {
            bgTex = new Texture("assets/images/backgrounds/Lawn.jpeg");
            bgTex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        } catch (Exception e) {
            Gdx.app.error("GameScreen", "Không tìm thấy file Frontyard.png!", e);
            bgTex = null;
        }
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(hudStage);
    }

    @Override
    public void render(float delta) {
        // Xóa màn hình
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        batch.setProjectionMatrix(camera.combined);

        // Vẽ world (background, sau này plant/zombie)
        batch.begin();
        if (bgTex != null) {
            // Vẽ phủ màn hình ảo 800x600
            batch.draw(bgTex, 0f, 0f, WORLD_WIDTH, WORLD_HEIGHT);
        }
        batch.end();

        // Vẽ HUD
        hudStage.act(delta);
        hudStage.draw();

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new ResumeScreen(game, this));
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
        if (bgTex != null) {
            bgTex.dispose();
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
