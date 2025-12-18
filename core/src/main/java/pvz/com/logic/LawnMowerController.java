package pvz.com.logic;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;

import pvz.com.entities.Zombies.BaseZombie;
import pvz.com.items.LawnMower;
import pvz.com.managers.GridConfig;
import pvz.com.managers.DesignConfig;

public class LawnMowerController {

    private final Array<LawnMower> lawnMowers = new Array<>();
    private final int laneCount = DesignConfig.ROWS;
    private final float worldWidth;
    private final float mowerStartX;

    
    private final Texture idleTexture;
    private final Texture activeTexture;

    public LawnMowerController(float worldWidth, float mowerStartX) {
        this.worldWidth = worldWidth;
        this.mowerStartX = mowerStartX;

        
        this.idleTexture = new Texture(Gdx.files.internal("images/items/lawnMower_Idle.png"));
        this.activeTexture = new Texture(Gdx.files.internal("images/items/lawnMower_Idle.png")); // Đổi đuôi sang png
        
        
        this.idleTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        this.activeTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
    }

    public void createLawnMowers() {
        disposeMowers(); 
        lawnMowers.clear();

        for (int row = 0; row < laneCount; row++) {
            float laneCenterY = GridConfig.getCellCenterY(row);
            
            float mowerY = laneCenterY - (DesignConfig.FIXED_WIDTH / 2f); 

            
            lawnMowers.add(new LawnMower(mowerStartX, mowerY, worldWidth, idleTexture, activeTexture));
        }
    }

    public void update(float delta, Array<BaseZombie> zombies) {
        for (int i = lawnMowers.size - 1; i >= 0; i--) {
            LawnMower mower = lawnMowers.get(i);
            mower.update(delta, zombies);

            
            if (mower.isUsed()) {
                lawnMowers.removeIndex(i);
            }
        }
    }

    public void render(SpriteBatch batch) {
        for (LawnMower mower : lawnMowers) {
            mower.render(batch);
        }
    }

    private void disposeMowers() {
        
        lawnMowers.clear();
    }

    public void dispose() {
        disposeMowers();
        
        if (idleTexture != null) idleTexture.dispose();
        if (activeTexture != null) activeTexture.dispose();
    }
}