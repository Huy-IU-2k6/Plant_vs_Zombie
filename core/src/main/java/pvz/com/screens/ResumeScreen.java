package pvz.com.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class ResumeScreen implements Screen {

    private final Game game;
    private final Screen previousScreen; // màn chơi trước đó (GameScreen)

    private Stage stage;

    private Texture bgTex;
    private Texture backTex;
    private Texture exitTex;

    private Image backgroundImage;
    private ImageButton backButton;
    private ImageButton exitButton;

    public ResumeScreen(Game game, Screen previousScreen) {
        this.game = game;
        this.previousScreen = previousScreen;

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        // Ảnh nền bia mộ
        bgTex = new Texture("assets/images/backgrounds/resume_bg.png"); // chính là hình có bia mộ
        backgroundImage = new Image(bgTex);
        backgroundImage.setFillParent(true);
        stage.addActor(backgroundImage);

        // Ảnh button
        backTex = new Texture("assets/images/buttons/back.png");
        exitTex = new Texture("assets/images/buttons/exit.png");

        backButton = new ImageButton(new TextureRegionDrawable(new TextureRegion(backTex)));
        exitButton = new ImageButton(new TextureRegionDrawable(new TextureRegion(exitTex)));

        // (tuỳ thích) chỉnh size cho nút
        backButton.setSize(820, 460);
        exitButton.setSize(720, 360);

        // Sự kiện
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(previousScreen); // resume
            }
        });

        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });

        // tí nữa mình đặt vị trí cho đúng “trên bia mộ”
        stage.addActor(backButton);
        stage.addActor(exitButton);
    }

    @Override
    public void show() {
        // mỗi lần hiển thị lại, set input cho stage
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(delta);
        stage.draw();
    }

    private void layoutButtons() {
        float stageW = stage.getViewport().getWorldWidth();
        float stageH = stage.getViewport().getWorldHeight();

        // Vùng bia mộ bên phải: dùng 1 chút “tỉ lệ” cho dễ scale theo màn hình
        float graveCenterX = stageW * 0.70f; // hơi lệch phải
        float graveCenterY = stageH * 0.45f; // ở giữa chiều cao

        float spacing = 20f;

        // Đặt Back ở trên, Exit ở dưới
        backButton.setPosition(
                graveCenterX - backButton.getWidth() / 2f,
                graveCenterY + spacing);

        exitButton.setPosition(
                graveCenterX - exitButton.getWidth() / 2f,
                graveCenterY - exitButton.getHeight() - spacing);
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        layoutButtons(); // cập nhật lại vị trí khi đổi size
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
        // Không dispose ở đây, để khi thật sự không dùng nữa mới dispose
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
