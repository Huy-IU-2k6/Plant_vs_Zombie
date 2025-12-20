package pvz.com;

import com.badlogic.gdx.Game;
import pvz.com.screens.MainMenuScreen;

public class Main extends Game {

    @Override
    public void create() {

        setScreen(new MainMenuScreen(this));
    }

    @Override
    public void render() {

        super.render();
    }

    @Override
    public void dispose() {

        super.dispose();
    }
}
