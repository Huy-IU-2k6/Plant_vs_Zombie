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

    private final Texture texture;
    private final PlantGridController grid;
    private final ShovelController shovelController;
    private final ISunReceiver sunReceiver;

    // UI config
    private float padding = 12f;
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

        this.texture = new Texture(Gdx.files.internal("images/items/Shovel.png"));

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
                moveGhostToStage(event, x, y); // đặt ghost đúng vị trí lúc bắt đầu

                // Quan trọng: giành touch focus để tiếp tục nhận drag
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

                // Thả ghost -> thử đào tại vị trí thả
                tryDigAtGhostPosition();

                // Xong thì tắt + ẩn ghost
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

        ghost = new GhostActor(new TextureRegion(texture));
        ghost.setSize(iconSize, iconSize);
        ghost.setOrigin(Align.center);
        ghost.setTouchable(Touchable.disabled); // ghost chỉ để vẽ

        stage.addActor(ghost);
    }

    private void moveGhostToStage(InputEvent event, float localX, float localY) {
        if (ghost == null)
            return;

        // localX/localY là tọa độ trong Shovel actor,
        // convert sang stage coords để đặt ghost
        float stageX = event.getStageX();
        float stageY = event.getStageY();

        // đặt ghost sao cho tâm ghost nằm ở con trỏ
        ghost.setPosition(stageX - ghost.getWidth() / 2f, stageY - ghost.getHeight() / 2f);
    }

    private void tryDigAtGhostPosition() {
        if (ghost == null)
            return;

        Stage stage = ghost.getStage();
        if (stage == null)
            return;

        // tâm ghost (stage coords)
        Vector2 v = new Vector2(
                ghost.getX() + ghost.getWidth() / 2f,
                ghost.getY() + ghost.getHeight() / 2f);

        // convert stage -> screen (screen coords chuẩn: (0,0) ở top-left)
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
    }

    public void setRefundRatio(float refundRatio) {
        this.refundRatio = Math.max(0f, refundRatio);
    }

    public void dispose() {
        texture.dispose();
    }

    // ===== Ghost actor: vẽ bóng mờ =====
    private static class GhostActor extends Actor {
        private final TextureRegion region;

        GhostActor(TextureRegion region) {
            this.region = region;
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            // alpha mờ
            float old = batch.getColor().a;
            batch.setColor(1f, 1f, 1f, 0.45f);
            batch.draw(region, getX(), getY(), getWidth(), getHeight());
            batch.setColor(1f, 1f, 1f, old);
        }
    }
}
