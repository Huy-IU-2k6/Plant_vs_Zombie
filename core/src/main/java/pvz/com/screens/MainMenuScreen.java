package pvz.com.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
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
import pvz.com.managers.SoundManager;

public class MainMenuScreen implements Screen {

    private final Game game;
    private final Stage stage;
    private final Table table;

    private final BackgroundManager backgroundManager;

    private Texture boardTex;

    // NEW: giữ reference để còn scale lại
    private TextButton startButton;
    private Cell<TextButton> startButtonCell;

    // Layout gốc em thiết kế trên 1920 x 1080
    private static final float BASE_SCREEN_W = 1920f;
    private static final float BASE_SCREEN_H = 1080f;

    // Kích thước board ở layout gốc (đo trong code cũ / photoshop)
    // giả sử em đang dùng height = 320 như trước
    private static final float BASE_BOARD_W = 690f; // nếu không chắc thì cứ đo gần đúng
    private static final float BASE_BOARD_H = 320f;

    // Tỉ lệ board trên màn hình gốc
    private static final float BOARD_H_SCREEN_RATIO = BASE_BOARD_H / BASE_SCREEN_H; // ~0.296f

    // Padding dưới của board + padding chữ trên board ở layout gốc
    private static final float BASE_TABLE_BOTTOM = 80f; // khoảng cách từ đáy màn -> đáy board
    private static final float BASE_LABEL_BOTTOM = 70f; // chữ cách đáy board
    private static final float BASE_FONT_SCALE = 1.0f;

    public MainMenuScreen(Game game) {
        this.game = game;

        stage = new Stage(new ScreenViewport());
        backgroundManager = new BackgroundManager();

        table = new Table();
        table.setFillParent(true);
        table.bottom(); // vẫn bám đáy
        stage.addActor(table);

        createUI();
        updateBoardLayout(); // scale lần đầu
    }

    private void createUI() {
        // Biển gỗ
        boardTex = new Texture("images/items/board.png");
        boardTex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        TextureRegionDrawable boardDrawable = new TextureRegionDrawable(new TextureRegion(boardTex));

        // Style cho button
        TextButton.TextButtonStyle startStyle = new TextButton.TextButtonStyle();
        startStyle.up = boardDrawable;
        startStyle.down = boardDrawable.tint(new Color(0.9f, 0.9f, 0.9f, 1f)); // nhấn xuống hơi tối
        startStyle.font = FontManager.getPvzFont();

        // Nút "CLICK TO START"
        startButton = new TextButton("CLICK TO START", startStyle);

        // Tạm thời không padBottom cứng, sẽ tính theo chiều cao của bảng
        startButton.getLabelCell().padBottom(0f);

        // Hiệu ứng nhấp nháy cho chữ
        startButton.getLabel().addAction(
                Actions.forever(
                        Actions.sequence(
                                Actions.fadeOut(0.6f),
                                Actions.fadeIn(0.6f))));

        // Thêm vào table, KHÔNG fix width/height cứng
        startButtonCell = table.add(startButton);

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

    /**
     * Scale & vị trí lại bảng theo kích thước màn hình hiện tại.
     */
    private void updateBoardLayout() {
        if (boardTex == null || startButtonCell == null || startButton == null)
            return;

        float worldW = stage.getViewport().getWorldWidth();
        float worldH = stage.getViewport().getWorldHeight();

        float texW = boardTex.getWidth();
        float texH = boardTex.getHeight();
        float aspect = texW / texH;

        // ===== SCALE BOARD DỰA TRÊN LAYOUT GỐC =====
        // cao luôn = (tỉ lệ board trên màn hình gốc) * chiều cao màn hình hiện tại
        float boardH = worldH * BOARD_H_SCREEN_RATIO;
        float boardW = boardH * aspect;

        // để chắc ăn, giữ luôn tỉ lệ chiếm chiều rộng giống layout gốc
        float maxBoardW = worldW * (BASE_BOARD_W / BASE_SCREEN_W);
        if (boardW > maxBoardW) {
            boardW = maxBoardW;
            boardH = boardW / aspect;
        }

        // set kích thước board (button)
        startButtonCell
                .width(boardW)
                .height(boardH);

        // ===== VỊ TRÍ BOARD =====
        // table.padBottom theo đúng tỉ lệ layout gốc
        float tableBottomRatio = BASE_TABLE_BOTTOM / BASE_SCREEN_H;
        table.padBottom(worldH * tableBottomRatio)
                .padTop(0f);

        // ===== SCALE & VỊ TRÍ CHỮ =====
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

        // Bật nhạc nền menu, loop
        SoundManager.i().playMusic("menu", true);

        // Đảm bảo layout chuẩn theo kích thước hiện tại
        updateBoardLayout();
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
        updateBoardLayout();
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
