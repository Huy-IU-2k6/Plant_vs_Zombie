package pvz.com.items;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;

import pvz.com.screens.GameScreen;

public class PlantCard extends Image {

    public final ItemType type;

    public static final float WIDTH = 95f;
    public static final float HEIGHT = 120f;

    private static final float DISABLED_ALPHA = 0.4f;
    private static final float ENABLED_ALPHA = 1f;

    private float cooldownRemaining = 0f;

    // Lock card until game starts
    private boolean lockedByGame = true;

    // Ghost image when dragging
    private Image dragGhost;

    public PlantCard(ItemType type) {
        super(new TextureRegionDrawable(
                new TextureRegion(new Texture(Gdx.files.internal(type.iconPath)))));

        this.type = type;

        setSize(WIDTH, HEIGHT);

        // Drag only, no click
        addDragSupport();

        updateStateUI();
    }

    /**
     * Get the GameScreen attached to stage.root.userObject
     */
    private GameScreen getGameScreen() {
        if (getStage() == null)
            return null;
        Object o = getStage().getRoot().getUserObject();
        return (o instanceof GameScreen) ? (GameScreen) o : null;
    }

    // ===================== DRAG & DROP =====================

    private void addDragSupport() {
        addListener(new DragListener() {
            @Override
            public void dragStart(InputEvent event, float x, float y, int pointer) {
                super.dragStart(event, x, y, pointer);

                if (getStage() == null)
                    return;

                GameScreen screen = getGameScreen();
                if (screen == null)
                    return;

                // [FIX] Get sun directly from GameScreen (GameWorld was removed)
                int currentSun = screen.getSunPoints();

                // If locked, cooldown active, or not enough sun -> cancel drag
                if (lockedByGame || cooldownRemaining > 0f || currentSun < type.cost) {
                    return;
                }

                // Create ghost card
                dragGhost = new Image(getDrawable());
                dragGhost.setSize(getWidth(), getHeight());
                dragGhost.setOrigin(Align.center);
                dragGhost.setColor(1f, 1f, 1f, 0.8f);

                getStage().addActor(dragGhost);
                updateGhostPosition(pointer);
            }

            @Override
            public void drag(InputEvent event, float x, float y, int pointer) {
                super.drag(event, x, y, pointer);
                if (dragGhost != null) {
                    updateGhostPosition(pointer);
                }
            }

            @Override
            public void dragStop(InputEvent event, float x, float y, int pointer) {
                super.dragStop(event, x, y, pointer);

                if (dragGhost != null) {
                    float screenX = Gdx.input.getX(pointer);
                    float screenY = Gdx.input.getY(pointer);

                    GameScreen screen = getGameScreen();
                    if (screen != null) {
                        // GameScreen handles conversion: screen -> world -> grid -> spawn plant
                        screen.onPlantCardDragged(PlantCard.this, screenX, screenY);
                    }

                    dragGhost.remove();
                    dragGhost = null;
                }
            }

            private void updateGhostPosition(int pointer) {
                if (getStage() == null || dragGhost == null)
                    return;

                float screenX = Gdx.input.getX(pointer);
                float screenY = Gdx.input.getY(pointer);

                // screen -> coordinates in hudStage (where card lives)
                Vector2 stageCoords = getStage().screenToStageCoordinates(
                        new Vector2(screenX, screenY));

                dragGhost.setPosition(
                        stageCoords.x - dragGhost.getWidth() / 2f,
                        stageCoords.y - dragGhost.getHeight() / 2f);
            }
        });
    }

    // ===================== GAME STATE / COOLDOWN =====================

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
        // If game hasn't started -> hide card
        if (lockedByGame) {
            setVisible(false);
            setTouchable(Touchable.disabled);
            return;
        }

        // Game started -> show card, manage visual state
        setVisible(true);

        boolean onCooldown = cooldownRemaining > 0f;
        setColor(1f, 1f, 1f, onCooldown ? DISABLED_ALPHA : ENABLED_ALPHA);
        setTouchable(onCooldown ? Touchable.disabled : Touchable.enabled);
    }
}