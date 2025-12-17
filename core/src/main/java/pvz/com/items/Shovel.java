package pvz.com.items;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.math.Vector2;

import pvz.com.entities.components.PlantTypeComponent;
import pvz.com.entities.plants.Plant;
import pvz.com.entities.plants.PlantType;
import pvz.com.logic.PlantGridController;
import pvz.com.logic.ShovelController;
import pvz.com.systems.ISunReceiver;

public class Shovel extends Actor {

    // icon (trong seedbank/HUD)
    private final Texture texture;

    // ghost texture (khi kéo/thả)
    private final Texture ghostTexture;
    private final TextureRegion ghostRegion;

    private final PlantGridController grid;
    private final ShovelController shovelController;
    private final ISunReceiver sunReceiver;

    // UI config
    private float padding = 6f;
    private float iconSize = 64f;

    // state
    private boolean active = false;
    private boolean dragging = false;

    // refund
    private float refundRatio = 1.0f;

    // ghost
    private GhostActor ghost;

    public Shovel(PlantGridController grid,
            ShovelController shovelController,
            ISunReceiver sunReceiver) {
        this.grid = grid;
        this.shovelController = shovelController;
        this.sunReceiver = sunReceiver;

        // Icon texture (khung xẻng)
        this.texture = new Texture(Gdx.files.internal("images/items/Shovel_Box.png"));

        // Ghost texture (xẻng thật)
        this.ghostTexture = new Texture(Gdx.files.internal("images/items/Shovel.png"));
        this.ghostRegion = new TextureRegion(ghostTexture);

        setSize(iconSize, iconSize);
        setTouchable(Touchable.enabled);

        // Kéo trực tiếp từ icon xẻng
        addListener(new DragListener() {

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (button != Input.Buttons.LEFT)
                    return false;

                active = true;
                dragging = true;

                ensureGhost(event.getStage());
                moveGhostToStage(event, x, y);

                return true;
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                if (!dragging)
                    return;
                moveGhostToStage(event, x, y);
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                if (!dragging)
                    return;

                dragging = false;

                tryDigAtGhostPosition();

                active = false;
                removeGhost();
            }
        });
    }

    private void ensureGhost(Stage stage) {
        if (stage == null)
            return;
        if (ghost != null && ghost.getStage() == stage)
            return;

        removeGhost();

        // ✅ ghost dùng Shovel.png
        ghost = new GhostActor(ghostRegion);
        ghost.setSize(iconSize, iconSize);
        ghost.setOrigin(Align.center);
        ghost.setTouchable(Touchable.disabled);

        stage.addActor(ghost);
    }

    private void moveGhostToStage(InputEvent event, float localX, float localY) {
        if (ghost == null)
            return;

        float stageX = event.getStageX();
        float stageY = event.getStageY();

        ghost.setPosition(stageX - ghost.getWidth() / 2f, stageY - ghost.getHeight() / 2f);
    }

    private void tryDigAtGhostPosition() {
        if (ghost == null)
            return;

        Stage stage = ghost.getStage();
        if (stage == null)
            return;

        Vector2 v = new Vector2(
                ghost.getX() + ghost.getWidth() / 2f,
                ghost.getY() + ghost.getHeight() / 2f);

        stage.stageToScreenCoordinates(v);

        int[] cell = grid.screenToNearestCell(Math.round(v.x), Math.round(v.y));
        if (cell == null || cell.length < 2)
            return;

        int row = cell[0], col = cell[1];
        if (row < 0 || col < 0)
            return;

        Plant plant = grid.getPlantAt(row, col);
        if (plant == null)
            return;

        // refund
        PlantType type = getPlantType(plant);
        if (type != null) {
            PlantDef def = PlantCatalog.def(type);
            int cost = def.cost();
            int refund = Math.max(0, Math.round(cost * refundRatio));
            if (refund > 0)
                sunReceiver.addSun(refund);
        }

        shovelController.tryRemovePlant(row, col);
    }

    private void removeGhost() {
        if (ghost != null) {
            ghost.remove();
            ghost = null;
        }
    }

    private PlantType getPlantType(Plant plant) {
        PlantTypeComponent tc = plant.getComponent(PlantTypeComponent.class);
        return (tc != null) ? tc.type : null;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        // icon vẫn vẽ Shovel_Box.png
        batch.draw(texture, getX(), getY(), getWidth(), getHeight());
    }

    public void layoutTopLeft(float hudWorldWidth, float hudWorldHeight) {
        setPosition(padding, hudWorldHeight - getHeight() - padding);
    }

    public void setPadding(float padding) {
        this.padding = padding;
    }

    public void setIconSize(float iconSize) {
        this.iconSize = iconSize;
        setSize(iconSize, iconSize);
        if (ghost != null)
            ghost.setSize(iconSize, iconSize);
    }

    public void setRefundRatio(float refundRatio) {
        this.refundRatio = Math.max(0f, refundRatio);
    }

    public void dispose() {
        texture.dispose();
        ghostTexture.dispose(); // ✅ nhớ dispose luôn ghost texture
    }

    // ===== Ghost actor: vẽ bóng mờ =====
    private static class GhostActor extends Actor {
        private final TextureRegion region;

        GhostActor(TextureRegion region) {
            this.region = region;
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            float old = batch.getColor().a;
            batch.setColor(1f, 1f, 1f, 0.45f);
            batch.draw(region, getX(), getY(), getWidth(), getHeight());
            batch.setColor(1f, 1f, 1f, old);
        }
    }
}
