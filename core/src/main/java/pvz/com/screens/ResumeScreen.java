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

import pvz.com.managers.ScaleManager;

public class ResumeScreen implements Screen {

    private final Game game;
    private final Screen previousScreen;

    private final Stage stage;

    private static final float SIGN_BASE_W = 450f;
    private static final float SIGN_BASE_H = 180f;
    private static final float SIGN_CENTER_X_RATIO = 0.05f;
    private static final float SIGN_CENTER_Y_RATIO = 0.23f;

    private static final float BACK_BASE_W = 650f;
    private static final float BACK_BASE_H = 260f;
    private static final float BACK_CENTER_Y_RATIO = 0.68f;
    private static final float BACK_CENTER_X_RATIO = 0.70f;

    private static final float EXIT_BASE_W = 650f;
    private static final float EXIT_BASE_H = 260f;
    private static final float EXIT_CENTER_Y_RATIO = 0.40f;
    private static final float EXIT_CENTER_X_RATIO = 0.70f;

    private final Texture bgTex;
    private final Texture backTex;
    private final Texture exitTex;
    private final Texture signTex;

    private final Image backgroundImage;
    private final Image signImage;
    private final ImageButton backButton;
    private final ImageButton exitButton;

    public ResumeScreen(Game game, Screen previousScreen) {
        this.game = game;
        this.previousScreen = previousScreen;

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        bgTex = new Texture("images/backgrounds/resume_bg.png");
        signTex = new Texture("images/items/Plants_vs_Zombies_logo.png");
        backTex = new Texture("images/buttons/back_button.png");
        exitTex = new Texture("images/buttons/exit_button.png");

        backgroundImage = new Image(bgTex);
        backgroundImage.setFillParent(true);
        stage.addActor(backgroundImage);

        signImage = new Image(signTex);
        stage.addActor(signImage);

        backButton = createButton(backTex, new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(previousScreen);
            }
        });

        exitButton = createButton(exitTex, new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });

        stage.addActor(backButton);
        stage.addActor(exitButton);

        layoutActors();
    }

    private ImageButton createButton(Texture texture, ClickListener listener) {
        ImageButton button = new ImageButton(
                new TextureRegionDrawable(new TextureRegion(texture)));
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

        float scale = ScaleManager.getHeightScale(stageH);

        float signW = SIGN_BASE_W * scale;
        float signH = SIGN_BASE_H * scale;
        signImage.setSize(signW, signH);
        centerActor(
                signImage,
                stageW * SIGN_CENTER_X_RATIO,
                stageH * SIGN_CENTER_Y_RATIO);

        float backW = BACK_BASE_W * scale;
        float backH = BACK_BASE_H * scale;
        backButton.setSize(backW, backH);
        centerActor(
                backButton,
                stageW * BACK_CENTER_X_RATIO,
                stageH * BACK_CENTER_Y_RATIO);

        float exitW = EXIT_BASE_W * scale;
        float exitH = EXIT_BASE_H * scale;
        exitButton.setSize(exitW, exitH);
        centerActor(
                exitButton,
                stageW * EXIT_CENTER_X_RATIO,
                stageH * EXIT_CENTER_Y_RATIO);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        layoutActors();
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
        layoutActors();
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
