package pvz.com.systems;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import pvz.com.entities.Entity;
import pvz.com.entities.components.*;
import pvz.com.entities.plants.bombs.PotatoMine;
import pvz.com.managers.GridConfig;

import java.util.List;

public class AnimationSystem {

    public void update(List<Entity> entities, float deltaTime) {
        for (Entity entity : entities) {
            AnimationComponent animComp = entity.getComponent(AnimationComponent.class);
            StateComponent stateComp = entity.getComponent(StateComponent.class);
            SpriteComponent spriteComp = entity.getComponent(SpriteComponent.class);

            if (animComp == null || stateComp == null || spriteComp == null)
                continue;

            stateComp.timeInState += deltaTime;
            EntityState currentState = stateComp.get();
            Animation<TextureRegion> animation = animComp.getAnimation(currentState);

            if (animation != null) {
                TextureRegion currentFrame = animation.getKeyFrame(stateComp.timeInState);
                spriteComp.setRegion(currentFrame);

                if (currentState == EntityState.GROWING) {
                    if (animation.isAnimationFinished(stateComp.timeInState)) {
                        stateComp.set(EntityState.UNARMED);
                        stateComp.timeInState = 0f;
                    }
                }

                if (currentState == EntityState.RISING) {

                    SizeComponent size = entity.getComponent(SizeComponent.class);

                    if (size != null && size.width < PotatoMine.BIG_WIDTH - 1f) {

                        size.width = PotatoMine.BIG_WIDTH;
                        size.height = PotatoMine.BIG_HEIGHT;

                        PositionComponent pos = entity.getComponent(PositionComponent.class);
                        GridCellComponent grid = entity.getComponent(GridCellComponent.class);

                        if (pos != null && grid != null) {
                            float centerX = GridConfig.getCellCenterX(grid.col);
                            float centerY = GridConfig.getCellCenterY(grid.row);

                            pos.x = centerX - (size.width / 2f);
                            pos.y = centerY - (size.height / 2f);
                        }
                    }

                    if (animation.isAnimationFinished(stateComp.timeInState)) {
                        stateComp.set(EntityState.IDLE);
                        stateComp.timeInState = 0f;
                    }
                }
            }
        }
    }
}
