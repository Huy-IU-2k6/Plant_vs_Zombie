package pvz.com.systems;

import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.List;

import pvz.com.entities.Entity;
import pvz.com.entities.Zombies.Zombies;
import pvz.com.entities.components.*;
import pvz.com.logic.PlantGridController;
import pvz.com.logic.ZombieWaveController;
import pvz.com.managers.GridConfig;

public class ExplosionSystem {
    private final ZombieWaveController zombieController;
    private final PlantGridController plantGridController;

    public ExplosionSystem(ZombieWaveController zombieController, PlantGridController plantGridController) {
        this.zombieController = zombieController;
        this.plantGridController = plantGridController;
    }

    public void update(List<Entity> entities, float delta) {
        List<Entity> toRemove = new ArrayList<>();

        for (Entity entity : entities) {
            ExplosiveComponent explosive = entity.getComponent(ExplosiveComponent.class);
            StateComponent state = entity.getComponent(StateComponent.class);
            PositionComponent pos = entity.getComponent(PositionComponent.class);
            AnimationComponent anim = entity.getComponent(AnimationComponent.class);
            SizeComponent size = entity.getComponent(SizeComponent.class);

            if (explosive == null || state == null || pos == null)
                continue;

            if (!explosive.hasExploded) {
                if (explosive.fuseTime >= 0) {
                    explosive.timer += delta;
                    if (explosive.timer >= explosive.fuseTime) {
                        state.set(EntityState.EXPLODING);
                    }
                }

                if (state.get() == EntityState.EXPLODING) {

                    explosive.hasExploded = true;

                    explosive.timer = 0f;

                    entity.removeComponent(HealthComponent.class);
                    entity.removeComponent(BoundsComponent.class);

                    if (plantGridController != null) {
                        int[] cell = GridConfig.worldToNearestCell(pos.x, pos.y);
                        int row = cell[0];
                        int col = cell[1];

                        plantGridController.unregisterPlantAtCell(row, col);
                    }

                    if (size != null) {
                        float oldSize = size.width;
                        float newSize = 250f;
                        float offset = (newSize - oldSize) / 2f;

                        pos.x -= offset;
                        pos.y -= offset;

                        size.width = newSize;
                        size.height = newSize;
                    }

                    dealAreaDamage(pos, size, explosive);
                }
            } else {
                explosive.timer += delta;

                float explodeAnimDuration = 0.8f;

                if (anim != null && anim.getAnimation(EntityState.EXPLODING) != null) {
                    explodeAnimDuration = anim.getAnimation(EntityState.EXPLODING).getAnimationDuration();
                }

                if (explosive.timer >= explodeAnimDuration) {
                    toRemove.add(entity);
                }
            }
        }

        entities.removeAll(toRemove);
    }

    private void dealAreaDamage(PositionComponent bombPos, SizeComponent size, ExplosiveComponent explosive) {
        float currentSize = (size != null) ? size.width : 90f;

        float centerX = bombPos.x + (currentSize / 2f);
        float centerY = bombPos.y + (currentSize / 2f);

        for (Zombies z : zombieController.getZombies()) {
            if (z.isDead())
                continue;

            float zCenterX = z.getX() + z.getWidth() / 2f;
            float zCenterY = z.getY() + z.getHeight() / 2f;

            float dist = Vector2.dst(centerX, centerY, zCenterX, zCenterY);

            if (dist <= explosive.range) {
                z.killByCherryBomb();
                z.setEating(false);
            }
        }
    }
}
