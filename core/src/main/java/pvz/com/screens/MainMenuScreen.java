package pvz.com.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;

import pvz.com.managers.BackgroundManager;
import pvz.com.managers.FontManager;

public class MainMenuScreen implements Screen {

    private final Game game;
    private final Stage stage;
    private final Table table;

    private final BackgroundManager backgroundManager;

    private Texture boardTex;

    private Music menuMusic;
    private Sound menuClick;
    private float musicVolume = 1f;
    private float sfxVolume = 1f;

    private TextButton startButton;
    private Cell<TextButton> startButtonCell;

    private static final float BASE_SCREEN_W = 1920f;
    private static final float BASE_SCREEN_H = 1080f;

    private static final float BASE_BOARD_W = 690f;
    private static final float BASE_BOARD_H = 320f;

    private static final float BOARD_H_SCREEN_RATIO = BASE_BOARD_H / BASE_SCREEN_H;

    private static final float BASE_TABLE_BOTTOM = 80f;
    private static final float BASE_LABEL_BOTTOM = 70f;
    private static final float BASE_FONT_SCALE = 1.0f;

    public MainMenuScreen(Game game) {
        this.game = game;

        stage = new Stage(new ScreenViewport());
        backgroundManager = new BackgroundManager();

        table = new Table();
        table.setFillParent(true);
        table.bottom();
        stage.addActor(table);

        loadAudio();

        createUI();
        updateBoardLayout();
    }

    private void loadAudio() {
        if (menuMusic == null) {
            menuMusic = Gdx.audio.newMusic(Gdx.files.internal("musics/grasswalk.mp3"));
            menuMusic.setLooping(true);
            menuMusic.setVolume(musicVolume);
        }
        if (menuClick == null) {
            menuClick = Gdx.audio.newSound(Gdx.files.internal("sounds/menu_click.mp3"));
        }
    }

    private void playMenuMusic() {
        if (menuMusic == null)
            return;
        menuMusic.setVolume(musicVolume);
        if (!menuMusic.isPlaying())
            menuMusic.play();
    }

    private void playClickSfx() {
        if (menuClick != null)
            menuClick.play(sfxVolume);
    }

    private Music takeMenuMusic() {
        Music m = menuMusic;
        menuMusic = null;
        return m;
    }

    private void createUI() {
        boardTex = new Texture("images/items/board_item.png");
        boardTex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        TextureRegionDrawable boardDrawable = new TextureRegionDrawable(new TextureRegion(boardTex));

        TextButton.TextButtonStyle startStyle = new TextButton.TextButtonStyle();
        startStyle.up = boardDrawable;
        startStyle.down = boardDrawable.tint(new Color(0.9f, 0.9f, 0.9f, 1f));
        startStyle.font = FontManager.getPvzFont();

        startButton = new TextButton("CLICK TO START", startStyle);
        startButton.getLabelCell().padBottom(0f);

        startButton.getLabel().addAction(
                Actions.forever(
                        Actions.sequence(
                                Actions.fadeOut(0.6f),
                                Actions.fadeIn(0.6f))));

        startButtonCell = table.add(startButton);

        startButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                playClickSfx();

                Music inheritedMenuMusic = takeMenuMusic();
                game.setScreen(new GameScreen(game, inheritedMenuMusic));
            }
        });
    }

    private void updateBoardLayout() {
        if (boardTex == null || startButtonCell == null || startButton == null)
            return;

        float worldW = stage.getViewport().getWorldWidth();
        float worldH = stage.getViewport().getWorldHeight();

        float texW = boardTex.getWidth();
        float texH = boardTex.getHeight();
        float aspect = texW / texH;

        float boardH = worldH * BOARD_H_SCREEN_RATIO;
        float boardW = boardH * aspect;

        float maxBoardW = worldW * (BASE_BOARD_W / BASE_SCREEN_W);
        if (boardW > maxBoardW) {
            boardW = maxBoardW;
            boardH = boardW / aspect;
        }

        startButtonCell.width(boardW).height(boardH);

        float tableBottomRatio = BASE_TABLE_BOTTOM / BASE_SCREEN_H;
        table.padBottom(worldH * tableBottomRatio).padTop(0f);

        float scaleFactor = boardH / BASE_BOARD_H;
        float fontScale = BASE_FONT_SCALE * scaleFactor;
        fontScale = Math.max(0.6f, Math.min(fontScale, 1.5f));
        startButton.getLabel().setFontScale(fontScale);

        float labelBottomRatio = BASE_LABEL_BOTTOM / BASE_BOARD_H;
        startButton.getLabelCell().padBottom(boardH * labelBottomRatio);

        table.invalidateHierarchy();
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        playMenuMusic();
        updateBoardLayout();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.05f, 0.25f, 0.05f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        Batch batch = stage.getBatch();
        batch.begin();
        backgroundManager.renderMenu(
                batch,
                stage.getViewport().getWorldWidth(),
                stage.getViewport().getWorldHeight());
        batch.end();

        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        updateBoardLayout();
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        stage.dispose();
        backgroundManager.dispose();

        if (boardTex != null) {
            boardTex.dispose();
            boardTex = null;
        }

        if (menuMusic != null) {
            menuMusic.dispose();
            menuMusic = null;
        }
        if (menuClick != null) {
            menuClick.dispose();
            menuClick = null;
        }
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    public void setMusicVolume(float v) {
        musicVolume = Math.max(0f, Math.min(1f, v));
        if (menuMusic != null)
            menuMusic.setVolume(musicVolume);
    }

    public void setSfxVolume(float v) {
        sfxVolume = Math.max(0f, Math.min(1f, v));
    }
}
