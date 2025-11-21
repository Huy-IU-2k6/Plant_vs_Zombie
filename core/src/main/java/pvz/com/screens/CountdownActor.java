package pvz.com.screens;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.graphics.g2d.Batch;

public class CountdownActor extends Actor {
    private float timeLeft; // tính bằng giây
    private BitmapFont font;

    public CountdownActor(float startTime, BitmapFont font) {
        this.timeLeft = startTime;
        this.font = font;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        timeLeft -= delta;
        if (timeLeft < 0)
            timeLeft = 0;
        // nếu timeLeft == 0 -> báo GameScreen bắt đầu spawn zombie chẳng hạn
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        font.draw(batch, "READY: " + (int) timeLeft, getX(), getY());
    }

    public boolean isFinished() {
        return timeLeft <= 0;
    }
}
