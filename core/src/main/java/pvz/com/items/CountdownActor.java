package pvz.com.items;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import pvz.com.managers.FontManager;
import pvz.com.managers.HudLayoutConfig;
import pvz.com.managers.ScaleManager;

public class CountdownActor extends Actor {

    private float timeLeft; 
    private final BitmapFont font;

    
    private static final float BASE_FONT_SCALE = 1.0f;

    
    private static final float COUNTDOWN_POS_X_DESIGN = HudLayoutConfig.COUNTDOWN_POS_X_DESIGN;
    private static final float COUNTDOWN_POS_Y_DESIGN = HudLayoutConfig.COUNTDOWN_POS_Y_DESIGN;

    public CountdownActor(float startTime) {
        this.timeLeft = startTime;
        this.font = FontManager.getPvzFont(); 
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        timeLeft -= delta;
        if (timeLeft < 0)
            timeLeft = 0;
        
    }

    
    public void updateLayout(float worldWidth, float worldHeight) {
        
        float x = ScaleManager.toWorldX(COUNTDOWN_POS_X_DESIGN, worldWidth);
        float y = ScaleManager.toWorldY(COUNTDOWN_POS_Y_DESIGN, worldHeight);

        setPosition(x, y);

        
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
