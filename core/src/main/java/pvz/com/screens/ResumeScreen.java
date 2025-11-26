package pvz.com.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class ResumeScreen implements Screen {

    private final Game game;
    private final Screen previousScreen;

    private final Stage stage;

    // Texture
    private final Texture bgTex;
    private final Texture backTex;
    private final Texture exitTex;
    private final Texture signTex;

    // Actor
    private final Image backgroundImage;
    private final Image signImage;
    private final ImageButton backButton;
    private final ImageButton exitButton;

    public ResumeScreen(Game game, Screen previousScreen) {
        this.game = game;
        this.previousScreen = previousScreen;

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        // Load texture
        bgTex = new Texture("assets/images/backgrounds/resume_bg.png");
        signTex = new Texture("assets/images/items/Plants_vs_Zombies_logo.png");
        backTex = new Texture("assets/images/buttons/back.png");
        exitTex = new Texture("assets/images/buttons/exit.png");

        // Background
        backgroundImage = new Image(bgTex);
        backgroundImage.setFillParent(true);
        stage.addActor(backgroundImage);

        // Logo / bảng
        signImage = new Image(signTex);
        signImage.setSize(450f, 180);
        stage.addActor(signImage);

        // Buttons
        backButton = createButton(backTex, 820f, 460f, new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(previousScreen);
            }
        });

        exitButton = createButton(exitTex, 720f, 360f, new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });

        stage.addActor(backButton);
        stage.addActor(exitButton);

        // Đặt vị trí lần đầu
        layoutActors();
    }

    private ImageButton createButton(Texture texture, float width, float height, ClickListener listener) {
        ImageButton button = new ImageButton(
                new TextureRegionDrawable(new TextureRegion(texture)));
        button.setSize(width, height);
        button.addListener(listener);
        return button;
    }

    private void centerActor(Actor actor, float centerX, float centerY) {
        actor.setPosition(
                centerX - actor.getWidth() / 2f,
                centerY - actor.getHeight() / 2f);
    }

    private void layoutActors() {
        float stageW = stage.getViewport().getWorldWidth();
        float stageH = stage.getViewport().getWorldHeight();

        float graveCenterX = stageW * 0.7f;
        float boardCenterX = stageW * 0.05f;

        float boardCenterY = stageH * 0.23f; // logo ở trên cao hơn
        float backCenterY = stageH * 0.65f; // nút Back
        float exitCenterY = stageH * 0.45f; // nút Exit

        // Logo
        centerActor(signImage, boardCenterX, boardCenterY);

        // Buttons
        centerActor(backButton, graveCenterX, backCenterY);
        centerActor(exitButton, graveCenterX, exitCenterY);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        layoutActors(); // resize xong đặt lại vị trí tất cả
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

    @Override
    public void dispose() {
        stage.dispose();
        bgTex.dispose();
        signTex.dispose();
        backTex.dispose();
        exitTex.dispose();
    }
}
