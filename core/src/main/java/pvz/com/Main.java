package pvz.com;

import com.badlogic.gdx.Game;
import pvz.com.screens.MainMenuScreen;

public class Main extends Game {

    @Override
    public void create() {
        // Khi game bắt đầu, hiển thị màn hình menu
        setScreen(new MainMenuScreen(this));
    }

    @Override
    public void render() {
        // Gọi lại render() mặc định của Game
        super.render();
    }

    @Override
    public void dispose() {
        // Game sẽ tự dispose screen hiện tại
        super.dispose();
    }
}
