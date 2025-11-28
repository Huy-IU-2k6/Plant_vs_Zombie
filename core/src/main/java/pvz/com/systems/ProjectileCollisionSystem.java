package pvz.com.systems;

import com.badlogic.gdx.math.Rectangle;
import java.util.ArrayList;
import java.util.List;

import pvz.com.entities.Entity;
import pvz.com.entities.components.BoundsComponent;
import pvz.com.entities.components.DamageComponent;
import pvz.com.entities.components.PositionComponent;
import pvz.com.entities.components.ProjectileTagComponent;
import pvz.com.entities.zombies.Zombies;
import pvz.com.logic.ZombieWaveController;

public class ProjectileCollisionSystem {

    private final List<Entity> entities;
    private final ZombieWaveController zombieWaveController;

    public ProjectileCollisionSystem(List<Entity> entities,
            ZombieWaveController zombieWaveController) {
        this.entities = entities;
        this.zombieWaveController = zombieWaveController;
    }

    public void update(float delta) {
        // 1. Lọc ra tất cả đạn
        List<Entity> projectiles = new ArrayList<>();
        for (Entity e : entities) {
            if (e.getComponent(ProjectileTagComponent.class) != null) {
                projectiles.add(e);
            }
        }

        List<Entity> toRemove = new ArrayList<>();

        // 2. Check từng viên đạn với tất cả zombie
        for (Entity projectile : projectiles) {
            BoundsComponent pBounds = projectile.getComponent(BoundsComponent.class);
            DamageComponent damage = projectile.getComponent(DamageComponent.class);
            if (pBounds == null || damage == null)
                continue;

            syncBoundsWithPosition(projectile);

            for (Zombies z : zombieWaveController.getZombies()) {
                if (z.isDead())
                    continue; // bỏ qua xác

                Rectangle zRect = z.getBounds(); // dùng hitBox trong class Zombies

                if (pBounds.bounds.overlaps(zRect)) {
                    // Trừ máu bằng API sẵn có
                    z.takeDamage(damage.amount);

                    // Đạn biến mất
                    toRemove.add(projectile);
                    break; // viên đạn này xong rồi
                }
            }
        }

        // 3. Xóa đạn đã nổ
        if (!toRemove.isEmpty()) {
            entities.removeAll(toRemove);
        }
    }

    private void syncBoundsWithPosition(Entity e) {
        PositionComponent pos = e.getComponent(PositionComponent.class);
        BoundsComponent bounds = e.getComponent(BoundsComponent.class);
        if (pos != null && bounds != null) {
            bounds.bounds.setPosition(pos.x, pos.y);
        }
    }
}
