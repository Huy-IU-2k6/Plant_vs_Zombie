package pvz.com.items;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Stage;

import pvz.com.managers.FontManager;
import pvz.com.managers.HudLayoutConfig;
import pvz.com.managers.ScaleManager;

public class CountdownActor extends Actor {

    private float timeLeft; // tính bằng giây
    private final BitmapFont font;

    // scale chữ ở layout gốc (BASE_SCREEN_H)
    private static final float BASE_FONT_SCALE = 1.0f;

    // Vị trí READY trên layout gốc (1920x1080)
    private static final float COUNTDOWN_POS_X_DESIGN = HudLayoutConfig.COUNTDOWN_POS_X_DESIGN;
    private static final float COUNTDOWN_POS_Y_DESIGN = HudLayoutConfig.COUNTDOWN_POS_Y_DESIGN;

    public CountdownActor(float startTime) {
        this.timeLeft = startTime;
        this.font = FontManager.getPvzFont(); // dùng chung HUD font
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        timeLeft -= delta;
        if (timeLeft < 0)
            timeLeft = 0;
        // nếu timeLeft == 0 -> có thể báo GameScreen bắt đầu spawn zombie
    }

    /**
     * Gọi hàm này khi biết worldWidth/worldHeight (vd trong
     * HudController.resize/updateLayout)
     */
    public void updateLayout(float worldWidth, float worldHeight) {
        // ===== SCALE VỊ TRÍ THEO TỈ LỆ MÀN HÌNH =====
        float x = ScaleManager.toWorldX(COUNTDOWN_POS_X_DESIGN, worldWidth);
        float y = ScaleManager.toWorldY(COUNTDOWN_POS_Y_DESIGN, worldHeight);

        setPosition(x, y);

        // ===== SCALE CHỮ THEO CHIỀU CAO MÀN HÌNH =====
        float heightScale = ScaleManager.getHeightScale(worldHeight);
        font.getData().setScale(BASE_FONT_SCALE * heightScale);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        Stage stage = getStage();
        if (stage == null)
            return;

        float drawX = getX();
        float drawY = getY();

        font.draw(batch, "READY: " + (int) timeLeft, drawX, drawY);
    }

    public boolean isFinished() {
        return timeLeft <= 0;
    }
}
