package pvz.com.entities;
import pvz.com.entities.components.*;
public  abstract class Entity {
    public PositionComponent position;
    public SizeComponent size;
    public BoundsComponent bounds;
    public SpriteComponent sprite;
    public HealthComponent health;
    public MovementComponent movement;
    public CooldownComponent cooldown;

    protected Entity() {

    }
}
