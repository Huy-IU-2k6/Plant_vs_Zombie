package pvz.com.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import pvz.com.Main; // Import lớp Main (extends Game) của bạn

public class MainMenuScreen implements Screen {

    // (1) Tham chiếu đến lớp Game (chính là file Main.java)
    // chúng ta dùng "Game" thay vì "Main" để code linh hoạt hơn
    private Game game;

    // (2) Sân khấu và Giao diện
    private Stage stage;
    private Skin skin;
    private Table table; // Dùng để sắp xếp các nút

    public MainMenuScreen(Game game) {
        this.game = game;

        // (3) Khởi tạo Stage với một Viewport
        // ScreenViewport giúp UI tự động co giãn theo cửa sổ
        stage = new Stage(new ScreenViewport());

        // (4) Tải Skin (Giao diện cho các nút)
        // !! QUAN TRỌNG: Bạn cần có file "uiskin.json" và "uiskin.atlas"
        // trong thư mục "assets" của bạn.
        try {
            skin = new Skin(Gdx.files.internal("skin/uiskin.json"));
        } catch (Exception e) {
            Gdx.app.error("MainMenuScreen", "Không tìm thấy file uiskin.json", e);
            // Tạo một skin rỗng nếu không tìm thấy để tránh crash
            skin = new Skin();
        }

        // (5) Khởi tạo Table để chứa các nút
        table = new Table();
        table.setFillParent(true); // Cho table lấp đầy màn hình
        stage.addActor(table); // Thêm table vào sân khấu

        // (6) Tạo các nút bấm (TextButton)
        TextButton playButton = new TextButton("Chơi (Play)", skin);
        TextButton exitButton = new TextButton("Thoát (Exit)", skin);

        // (7) Thêm các nút vào Table
        table.add(playButton).fillX().uniformX().pad(10);
        table.row(); // Xuống hàng mới
        table.add(exitButton).fillX().uniformX().pad(10);

        // (8) Thêm sự kiện Click cho nút "Chơi"
        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("MainMenu", "Nút CHƠI được nhấn!");
                // !! Đây là dòng chuyển màn hình
                // Bạn sẽ cần tạo file "GameScreen.java"
                // game.setScreen(new GameScreen((Main) game));
            }
        });

        // (9) Thêm sự kiện Click cho nút "Thoát"
        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("MainMenu", "Nút THOÁT được nhấn!");
                Gdx.app.exit(); // Thoát game
            }
        });
    }

    @Override
    public void show() {
        // (10) Báo cho LibGDX biết Stage sẽ nhận input (click chuột, gõ phím)
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        // (11) Xóa màn hình
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // (12) Cập nhật và vẽ sân khấu (Stage)
        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        // (13) Cập nhật viewport khi cửa sổ thay đổi kích thước
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {
        // (14) Khi màn hình bị ẩn đi (ví dụ: chuyển sang GameScreen),
        // ta nên gỡ InputProcessor
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        // (15) Giải phóng tài nguyên khi màn hình bị hủy
        stage.dispose();
        skin.dispose();
    }
}
