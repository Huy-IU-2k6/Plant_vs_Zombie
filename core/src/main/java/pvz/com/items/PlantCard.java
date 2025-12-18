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
import pvz.com.entities.plants.PlantType;
import pvz.com.logic.HudController;
import pvz.com.screens.GameScreen;

public class PlantCard extends Image {

    public final PlantType type;
    private final PlantDef def;

    public static final float WIDTH = 109f;
    public static final float HEIGHT = 120f;

    private static final float DISABLED_ALPHA = 0.4f;
    private static final float ENABLED_ALPHA = 1f;

    private float cooldownRemaining = 0f;
    private boolean lockedByGame = true;

    
    private Image dragGhost;

    public PlantCard(PlantType type) {
        super(makeDrawable(type));

        this.type = type;
        this.def = PlantCatalog.def(type);

        setSize(WIDTH, HEIGHT);

        
        addDragSupport();

        updateStateUI();
    }

    private static TextureRegionDrawable makeDrawable(PlantType type) {
        PlantDef def = PlantCatalog.def(type);
        return new TextureRegionDrawable(new TextureRegion(new Texture(Gdx.files.internal(def.iconPath()))));
    }

    public PlantDef getDef() {
        return def;
    }

    
    private GameScreen getGameScreen() {
        if (getStage() == null)
            return null;
        Object o = getStage().getRoot().getUserObject();
        return (o instanceof GameScreen) ? (GameScreen) o : null;
    }

    private HudController getHudController() {
        GameScreen screen = getGameScreen();
        return (screen != null) ? screen.getHudController() : null;
    }



    private void addDragSupport() {
        addListener(new DragListener() {

            private boolean dragAccepted = false;

            @Override
            public void dragStart(InputEvent event, float x, float y, int pointer) {
                super.dragStart(event, x, y, pointer);

                dragAccepted = false;

                if (getStage() == null)
                    return;

                HudController hud = getHudController();
                if (hud == null)
                    return;

                int currentSun = hud.getSunPoints();

                
                if (lockedByGame || cooldownRemaining > 0f || currentSun < def.cost()) {
                    return;
                }

                
                dragGhost = new Image(getDrawable());
                dragGhost.setSize(getWidth(), getHeight());
                dragGhost.setOrigin(Align.center);
                dragGhost.setColor(1f, 1f, 1f, 0.8f);

                getStage().addActor(dragGhost);
                updateGhostPosition(pointer);

                dragAccepted = true;
            }

            @Override
            public void drag(InputEvent event, float x, float y, int pointer) {
                super.drag(event, x, y, pointer);
                if (dragGhost != null)
                    updateGhostPosition(pointer);
            }

            @Override
            public void dragStop(InputEvent event, float x, float y, int pointer) {
                super.dragStop(event, x, y, pointer);

                if (!dragAccepted) {
                    cleanupGhost();
                    return;
                }

                if (dragGhost != null) {
                    float screenX = Gdx.input.getX(pointer);
                    float screenY = Gdx.input.getY(pointer);

                    GameScreen screen = getGameScreen();
                    if (screen != null) {
                        screen.onPlantCardDragged(PlantCard.this, screenX, screenY);
                    }

                    cleanupGhost();
                }
            }

            private void cleanupGhost() {
                if (dragGhost != null) {
                    dragGhost.remove();
                    dragGhost = null;
                }
            }

            private void updateGhostPosition(int pointer) {
                if (getStage() == null || dragGhost == null)
                    return;

                float screenX = Gdx.input.getX(pointer);
                float screenY = Gdx.input.getY(pointer);

                Vector2 stageCoords = getStage().screenToStageCoordinates(new Vector2(screenX, screenY));

                dragGhost.setPosition(
                        stageCoords.x - dragGhost.getWidth() / 2f,
                        stageCoords.y - dragGhost.getHeight() / 2f);
            }
        });
    }

    

    public void setLockedByGame(boolean locked) {
        this.lockedByGame = locked;
        updateStateUI();
    }

    public boolean canUse(int sun) {
        return !lockedByGame && sun >= def.cost() && cooldownRemaining <= 0f;
    }

    public void triggerUse() {
        cooldownRemaining = def.cooldown();
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
        if (lockedByGame) {
            setVisible(false);
            setTouchable(Touchable.disabled);
            return;
        }

        setVisible(true);

        boolean onCooldown = cooldownRemaining > 0f;
        setColor(1f, 1f, 1f, onCooldown ? DISABLED_ALPHA : ENABLED_ALPHA);
        setTouchable(onCooldown ? Touchable.disabled : Touchable.enabled);
    }
}
