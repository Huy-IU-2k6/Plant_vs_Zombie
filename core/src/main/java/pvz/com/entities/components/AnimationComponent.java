package pvz.com.entities.components;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import java.util.HashMap;
import java.util.Map;

public class AnimationComponent {
    
    public Map<EntityState, Animation<TextureRegion>> animations;

    public AnimationComponent() {
        animations = new HashMap<>();
    }

    
    public void addAnimation(EntityState state, Animation<TextureRegion> animation) {
        animations.put(state, animation);
    }

    
    public Animation<TextureRegion> getAnimation(EntityState state) {
        return animations.get(state);
    }
}