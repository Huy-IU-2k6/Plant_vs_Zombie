package pvz.com.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class MainMenuScreen implements Screen {

    private final Game game;

    private final Stage stage;
    private final Table table;

    private Skin skin;
    private Texture bgTex;
    private BitmapFont titleFont;

    public MainMenuScreen(Game game) {
        this.game = game;

        stage = new Stage(new ScreenViewport());
        table = new Table();
        table.setFillParent(true);
        table.center();

        loadSkin();
        createBackground();
        createUI();
    }

    private void loadSkin() {
        try {
            skin = new Skin(Gdx.files.internal("skin/uiskin.json"));
        } catch (Exception e) {
            Gdx.app.error("MainMenuScreen", "Không tìm thấy file uiskin.json", e);
            skin = new Skin();
        }
    }

    private void createBackground() {
        bgTex = new Texture("assets/images/items/Frontyard.png");
        bgTex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        Image bg = new Image(bgTex);
        bg.setFillParent(true);

        stage.addActor(bg);
        stage.addActor(table);
    }

    private void createUI() {
        // Font title từ TTF
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(
                Gdx.files.internal("assets/fonts/Roboto-Black.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter param = new FreeTypeFontGenerator.FreeTypeFontParameter();
        param.size = 70;
        titleFont = generator.generateFont(param);
        generator.dispose();

        // Title label
        Label.LabelStyle titleStyle = new Label.LabelStyle();
        titleStyle.font = titleFont;
        titleStyle.fontColor = new Color(0.95f, 1f, 0.4f, 1f);

        Label title = new Label("Plants vs Zombies", titleStyle);

        // Buttons
        TextButton playButton = new TextButton("Play", skin);
        TextButton exitButton = new TextButton("Exit", skin);

        // Layout
        table.add(title).padBottom(40f);
        table.row();
        table.add(playButton).width(220).height(50).pad(10f);
        table.row();
        table.add(exitButton).width(220).height(50).pad(10f);

        // Listeners
        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("MainMenu", "Play button clicked");
                game.setScreen(new GameScreen(game));
                dispose();
            }
        });

        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("MainMenu", "Exit button clicked");
                Gdx.app.exit();
            }
        });
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.05f, 0.25f, 0.05f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        stage.dispose();
        if (skin != null) {
            skin.dispose();
        }
        if (bgTex != null) {
            bgTex.dispose();
        }
        if (titleFont != null) {
            titleFont.dispose();
        }
    }
}
