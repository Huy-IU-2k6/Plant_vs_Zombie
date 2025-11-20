package pvz.com.screens;

import com.badlogic.gdx.scenes.scene2d.actions.Actions;
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
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class MainMenuScreen implements Screen {

    private final Game game;
    private final Stage stage;
    private final Table table;

    private Texture bgTex;
    private Texture boardTex;
    private BitmapFont pvzFont;

    public MainMenuScreen(Game game) {
        this.game = game;

        stage = new Stage(new ScreenViewport());

        table = new Table();
        table.setFillParent(true);
        table.bottom();
        table.padBottom(55f);

        createBackground();
        createFont();
        createUI();
    }

    private void createBackground() {
        bgTex = new Texture("assets/images/backgrounds/wellcome-background.png");
        bgTex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        Image bg = new Image(bgTex);
        bg.setFillParent(true);

        stage.addActor(bg);
        stage.addActor(table);
    }

    private void createFont() {
        FreeTypeFontGenerator gen = new FreeTypeFontGenerator(
                Gdx.files.internal("fonts/HouseofTerror/HouseofTerror Regular.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter param = new FreeTypeFontGenerator.FreeTypeFontParameter();

        param.size = 50;

        // Màu chữ kem
        param.color = new Color(0xFBE3B5FF); // hơi vàng, giống hình

        // Viền nâu đậm quanh chữ
        param.borderWidth = 3f; // tăng/giảm để viền dày/mỏng
        param.borderColor = new Color(0xA46B3AFF);
        param.borderStraight = true; // viền sắc nét hơn

        // Bóng mờ nhẹ làm chữ nổi lên
        param.shadowOffsetX = 2;
        param.shadowOffsetY = -2; // đổi dấu nếu hướng bóng sai
        param.shadowColor = new Color(0f, 0f, 0f, 0.35f); // đen, nhưng trong suốt

        pvzFont = gen.generateFont(param);
        gen.dispose();

        // (không bắt buộc) giúp font bớt răng cưa khi phóng to
        pvzFont.getRegion().getTexture().setFilter(
                Texture.TextureFilter.Linear,
                Texture.TextureFilter.Linear);
    }

    private void createUI() {
        // Biển gỗ
        boardTex = new Texture("assets/images/items/board.png");
        boardTex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        TextureRegionDrawable boardDrawable = new TextureRegionDrawable(new TextureRegion(boardTex));

        // Style cho button
        TextButton.TextButtonStyle startStyle = new TextButton.TextButtonStyle();
        startStyle.up = boardDrawable;
        startStyle.down = boardDrawable.tint(new Color(0.9f, 0.9f, 0.9f, 1f)); // nhấn xuống hơi tối
        startStyle.font = pvzFont;

        // Nút "CLICK TO START"
        TextButton startButton = new TextButton("CLICK TO START", startStyle);
        startButton.getLabel().setFontScale(1.0f);

        startButton.getLabelCell().padBottom(70f); // 10f, 15f, 20f… thử tăng dần

        // hiệu ứng nhấp nháy cho chữ
        startButton.getLabel().addAction(
                Actions.forever(
                        Actions.sequence(
                                Actions.fadeOut(0.6f), // mờ dần trong 0.6s
                                Actions.fadeIn(0.6f) // sáng dần trong 0.6s
                        )));

        table.add(startButton)
                .width(500f) // rộng hơn texture gốc
                .height(320f) // cao hơn
                .padTop(20f);

        // Click -> sang GameScreen
        startButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new GameScreen(game));
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
    public void dispose() {
        stage.dispose();

        if (bgTex != null) {
            bgTex.dispose();
        }
        if (boardTex != null) {
            boardTex.dispose();
        }
        if (pvzFont != null) {
            pvzFont.dispose();
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
        Gdx.input.setInputProcessor(null);
    }
}
