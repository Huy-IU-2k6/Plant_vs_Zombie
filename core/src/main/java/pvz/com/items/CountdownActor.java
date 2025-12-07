package pvz.com.items;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Stage;

import pvz.com.managers.FontManager;

public class CountdownActor extends Actor {

    private float timeLeft; // tính bằng giây
    private final BitmapFont font;

    public CountdownActor(float startTime) {
        this.timeLeft = startTime;
        this.font = FontManager.getPvzFont(); // <-- dùng FontManager
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        timeLeft -= delta;
        if (timeLeft < 0)
            timeLeft = 0;
        // nếu timeLeft == 0 -> có thể báo GameScreen bắt đầu spawn zombie
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        Stage stage = getStage();
        if (stage == null)
            return;

        float worldWidth = stage.getViewport().getWorldWidth();
        float worldHeight = stage.getViewport().getWorldHeight();

        // ===== SCALE FONT THEO TỶ LỆ MÀN HÌNH (dùng FontManager) =====
        float fontScale = FontManager.computeHudFontScale(worldWidth, worldHeight);

        // Lưu scale cũ (vì font dùng chung nhiều chỗ)
        float oldScaleX = font.getData().scaleX;
        float oldScaleY = font.getData().scaleY;

        font.getData().setScale(fontScale);

        // ===== VỊ TRÍ: DÙNG WORLD COORD TRỰC TIẾP =====
        // HudController / HudLayoutConfig sẽ setPosition() sẵn
        float drawX = getX();
        float drawY = getY();

        font.draw(batch, "READY: " + (int) timeLeft, drawX, drawY);

        // khôi phục scale cũ cho các chỗ khác dùng font này
        font.getData().setScale(oldScaleX, oldScaleY);
    }

    public boolean isFinished() {
        return timeLeft <= 0;
    }
}
