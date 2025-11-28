package pvz.com.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;

import pvz.com.managers.BackgroundManager;
import pvz.com.managers.FontManager;
import pvz.com.managers.SoundManager; // ★ NEW

public class MainMenuScreen implements Screen {

    private final Game game;
    private final Stage stage;
    private final Table table;

    private final BackgroundManager backgroundManager;

    private Texture boardTex;

    public MainMenuScreen(Game game) {
        this.game = game;

        stage = new Stage(new ScreenViewport());
        backgroundManager = new BackgroundManager();

        table = new Table();
        table.setFillParent(true);
        table.bottom();
        table.padBottom(55f);

        stage.addActor(table);

        createUI();
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
        startStyle.font = FontManager.getPvzFont();

        // Nút "CLICK TO START"
        TextButton startButton = new TextButton("CLICK TO START", startStyle);
        startButton.getLabel().setFontScale(1.0f);

        // Đẩy text lên cao trên tấm biển
        startButton.getLabelCell().padBottom(70f);

        // Hiệu ứng nhấp nháy cho chữ
        startButton.getLabel().addAction(
                Actions.forever(
                        Actions.sequence(
                                Actions.fadeOut(0.6f),
                                Actions.fadeIn(0.6f))));

        table.add(startButton)
                .width(500f)
                .height(320f)
                .padTop(20f);

        // Click -> sang GameScreen
        startButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // phát sound click trước
                SoundManager.i().playSound("menu_click");

                // rồi chuyển màn
                game.setScreen(new GameScreen(game));
            }
        });
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);

        // ★ NEW: bật nhạc nền menu, loop
        SoundManager.i().playMusic("menu", true);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.05f, 0.25f, 0.05f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Vẽ background bằng BackgroundManager
        Batch batch = stage.getBatch();
        batch.begin();
        backgroundManager.renderMenu(
                batch,
                stage.getViewport().getWorldWidth(),
                stage.getViewport().getWorldHeight());
        batch.end();

        // Vẽ UI
        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose(); // huỷ actors + batch
        backgroundManager.dispose(); // huỷ background textures

        if (boardTex != null) {
            boardTex.dispose();
            boardTex = null;
        }
        // Không dispose SoundManager ở đây, để Game chính quản lý.
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
