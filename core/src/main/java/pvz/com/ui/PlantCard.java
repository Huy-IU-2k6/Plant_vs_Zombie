package pvz.com.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

import pvz.com.screens.GameScreen;

public class PlantCard extends Image {

    public final ItemType type;

    public static final float WIDTH = 95f;
    public static final float HEIGHT = 120f;

    private static final float DISABLED_ALPHA = 0.4f;
    private static final float ENABLED_ALPHA = 1f;

    private float cooldownRemaining = 0f;

    // >>> NEW: khoá card cho đến khi game start xong
    private boolean lockedByGame = true;

    public PlantCard(ItemType type) {
        super(new TextureRegionDrawable(
                new TextureRegion(new Texture(Gdx.files.internal(type.iconPath)))));

        this.type = type;

        setSize(WIDTH, HEIGHT);

        addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                GameScreen screen = getGameScreen();
                if (screen != null) {
                    screen.onPlantCardClicked(PlantCard.this);
                }
            }
        });

        updateStateUI();
    }

    private GameScreen getGameScreen() {
        if (getStage() == null)
            return null;
        Object o = getStage().getRoot().getUserObject();
        return (o instanceof GameScreen) ? (GameScreen) o : null;
    }

    // >>> NEW: cho GameScreen mở/khoá card
    public void setLockedByGame(boolean locked) {
        this.lockedByGame = locked;
        updateStateUI();
    }

    public boolean canUse(int sun) {
        return !lockedByGame && sun >= type.cost && cooldownRemaining <= 0f;
    }

    public void triggerUse() {
        cooldownRemaining = type.cooldown;
        updateStateUI();
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        if (cooldownRemaining > 0f) {
            cooldownRemaining = Math.max(0f, cooldownRemaining - delta);
            updateStateUI();
        }
    }

    private void updateStateUI() {
        // Nếu game chưa cho dùng (đang countdown) -> ẩn hẳn card
        if (lockedByGame) {
            setVisible(false);
            setTouchable(Touchable.disabled);
            return;
        }

        // Game đã bắt đầu -> hiện lên, nhưng vẫn quản lý cooldown như cũ
        setVisible(true);

        boolean onCooldown = cooldownRemaining > 0f;
        setColor(1f, 1f, 1f, onCooldown ? DISABLED_ALPHA : ENABLED_ALPHA);
        setTouchable(onCooldown ? Touchable.disabled : Touchable.enabled);
    }
}
