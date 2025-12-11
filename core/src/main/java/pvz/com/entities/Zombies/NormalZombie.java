package pvz.com.entities.Zombies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

import pvz.com.managers.GifManager;
import pvz.com.managers.DesignConfig;
import pvz.com.managers.ScaleManager;

// Ensure this class implements Disposable as per our previous fix
public class NormalZombie extends Zombies {

    // ===== CONST =====
    private static final int BODY_HEALTH = 100;
    private static final float INITIAL_SPEED = 10f; // Using INITIAL_SPEED from HEAD

    private static final int FRAMES_PER_ROW = 1;
    
    // Animation Speeds
    private static final float WALK_FRAME_TIME = 0.12f; 
    private static final float EAT_FRAME_TIME = 0.25f;
    private static final float BODY_DIE_FRAME_TIME = 0.15f; 
    private static final float HEAD_POP_FRAME_TIME = 0.1f;  

    // ===== TEXTURES =====
    private final Array<Texture> walkTextures;
    private final Array<Texture> headPopTextures;
    private final Array<Texture> bodyDieTextures;
    private final Array<Texture> eatTextures; // New texture array for Eat animation

    // ===== ANIMATIONS =====
    private final Animation<TextureRegion> walkAnim;
    private final Animation<TextureRegion> headPopAnim; 
    private final Animation<TextureRegion> bodyDieAnim; 
    private final Animation<TextureRegion> eatAnim;

    // ===== STATE =====
    private float stateTime = 0f;
    private boolean isDying = false;
    private boolean isEating = false;
    
    private boolean sizeInitialized = false;
    private float originalW;
    private float originalH;

    public NormalZombie() {
        super();

        // 1. ===== LOAD WALK ANIMATION (PNG Sequence) =====
        walkTextures = new Array<>();
        // Adjust the range based on your actual file names
        for (int i = 0; i <= 21; i++) {
            walkTextures.add(new Texture("images/Zombies/NormalZombie/Zombie/Zombie_" + i + ".png"));
        }
        walkAnim = createAnimation(walkTextures, WALK_FRAME_TIME, Animation.PlayMode.LOOP);

        // 2. ===== LOAD HEAD POP ANIMATION =====
        headPopTextures = new Array<>();
        // Adjust the range based on your actual file names
        for (int i = 0; i <= 11; i++) {
            headPopTextures.add(new Texture("images/Zombies/NormalZombie/ZombieHead/ZombieHead_" + i + ".png"));
        }
        headPopAnim = createAnimation(headPopTextures, HEAD_POP_FRAME_TIME, Animation.PlayMode.NORMAL);

        // 3. ===== LOAD BODY DIE ANIMATION =====
        bodyDieTextures = new Array<>();
        // Load LostHead frames first
        for (int i = 0; i <= 17; i++) {
             bodyDieTextures.add(new Texture("images/Zombies/NormalZombie/ZombieLostHead/ZombieLostHead_" + i + ".png"));
        }
        // Then load Die frames
        for (int i = 0; i <= 9; i++) {
             bodyDieTextures.add(new Texture("images/Zombies/NormalZombie/ZombieDie/ZombieDie_" + i + ".png"));
        }
        bodyDieAnim = createAnimation(bodyDieTextures, BODY_DIE_FRAME_TIME, Animation.PlayMode.NORMAL);

        // 4. ===== LOAD EAT ANIMATION (PNG Sequence) =====
        eatTextures = new Array<>();
        // Assuming you have eat frames, e.g., ZombieEat_0.png to ZombieEat_10.png
        // If not, you can reuse walk frames temporarily or load your specific eat frames here
        // For now, I'll reuse the first walk frame as a placeholder if you don't have eat frames
        // BUT ideally, load your real eat frames:
         for (int i = 0; i <= 10; i++) {
            eatTextures.add(new Texture("images/Zombies/NormalZombie/ZombieAttack/ZombieAttack_" + i + ".png"));
         }
        // Placeholder using walk frame 0:
       
        eatAnim = createAnimation(eatTextures, EAT_FRAME_TIME, Animation.PlayMode.LOOP);

        // 5. ===== INIT SIZE & STATS =====
        TextureRegion firstFrame = walkAnim.getKeyFrame(0f);
        originalW = firstFrame.getRegionWidth();
        originalH = firstFrame.getRegionHeight();
        
        // Initial size (will be rescaled by initSizeIfNeeded)
        setSize(originalW, originalH); 

        this.health = BODY_HEALTH;
        this.baseSpeed = INITIAL_SPEED;
        this.speed = this.baseSpeed;
    }

    private Animation<TextureRegion> createAnimation(Array<Texture> textures, float frameDuration, Animation.PlayMode mode) {
        TextureRegion[] frames = new TextureRegion[textures.size];
        for (int i = 0; i < textures.size; i++) {
            frames[i] = new TextureRegion(textures.get(i));
        }
        Animation<TextureRegion> anim = new Animation<>(frameDuration, frames);
        anim.setPlayMode(mode);
        return anim;
    }
    
    private void initSizeIfNeeded() {
        if (sizeInitialized) return;

        float worldHeight = (getStage() != null && getStage().getViewport() != null) 
                            ? getStage().getViewport().getWorldHeight() 
                            : ScaleManager.BASE_SCREEN_H;

        // Scale logic using DesignConfig.ZOMBIE_H
        float zombieWorldH = ScaleManager.scaleByHeight(DesignConfig.ZOMBIE_H, worldHeight);
        float aspect = originalW / originalH;
        float zombieWorldW = zombieWorldH * aspect;

        setSize(zombieWorldW, zombieWorldH);
        sizeInitialized = true;
    }

    @Override
    public void act(float delta) {
        initSizeIfNeeded();

        // --- DEATH LOGIC ---
        if (isDying) {
            stateTime += delta;
            
            // Check if BODY has finished falling
            if (bodyDieAnim.isAnimationFinished(stateTime)) {
                if (!dead) { 
                    dead = true; 
                    speed = 0f;
                    if (zombieCount > 0) zombieCount--;
                    remove(); 
                }
            }
            return;
        }

        super.act(delta);
        
        // --- ANIMATION UPDATE ---
        if (isEating) {
            stateTime += delta;
        } else {
            // Snow Pea Slow Effect
            float animSpeedScale = (this.speed > 0) ? (this.speed / this.baseSpeed) : 1f;
            if (animSpeedScale < 0.2f) animSpeedScale = 1f; 
            stateTime += delta * animSpeedScale;
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (dead) return;

        Color color = getColor();
        batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);

        if (isDying) {
            // 1. Draw BODY (Always)
            TextureRegion bodyFrame = bodyDieAnim.getKeyFrame(stateTime);
            batch.draw(bodyFrame, getX(), getY(), getWidth(), getHeight());

            // 2. Draw HEAD (Only if not finished)
            if (!headPopAnim.isAnimationFinished(stateTime)) {
                TextureRegion headFrame = headPopAnim.getKeyFrame(stateTime);
                batch.draw(headFrame, getX(), getY(), getWidth(), getHeight());
            }
        } else {
            Animation<TextureRegion> currentAnim = isEating ? eatAnim : walkAnim;
            TextureRegion frame = currentAnim.getKeyFrame(stateTime);
            batch.draw(frame, getX(), getY(), getWidth(), getHeight());
        }

        batch.setColor(Color.WHITE);
    }

    private void startDeath() {
        if (isDying || dead) return;
        isDying = true;
        stateTime = 0f;
        health = 0;
        speed = 0f;
        setColor(Color.WHITE);
    }

    @Override
    public void takeDamage(int damage) {
        if (isDying || dead) return;
        health -= damage;
        if (health <= 0) startDeath();
    }

    @Override
    public void killByMower() { startDeath(); }
    @Override
    public void killByCherryBomb() { startDeath(); }

    @Override
    public boolean isEating() { return isEating; }

    public void setEating(boolean eating) {
        if (isDying || dead) return;
        if (this.isEating == eating) return;

        this.isEating = eating;
        stateTime = 0f;

        if (eating) {
            this.speed = 0f;
        } else {
            this.speed = this.baseSpeed;
        }
    }

   
    public void dispose() {
        if (walkTextures != null) for (Texture t : walkTextures) t.dispose();
        if (headPopTextures != null) for (Texture t : headPopTextures) t.dispose();
        if (bodyDieTextures != null) for (Texture t : bodyDieTextures) t.dispose();
        if (eatTextures != null) for (Texture t : eatTextures) t.dispose();
        // eatSheet was replaced by eatTextures, so remove its dispose if not used
    }
}