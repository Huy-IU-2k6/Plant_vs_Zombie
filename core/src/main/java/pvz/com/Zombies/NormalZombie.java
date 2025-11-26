package pvz.com.Zombies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.audio.Sound;

import pvz.com.managers.GifManager;

/**
 * NormalZombie built following SOLID + Supports CherryBomb Burn Death
 */
public class NormalZombie extends Zombies {

    // ----------- CONSTANTS -------------
    private static final int MAX_HEALTH = 100;
    private static final float MOVE_SPEED = 50f;

    // ----------- STATE -------------
    private ZombieState state = ZombieState.WALK;
    private float stateTime = 0f;

    // ----------- COMPONENTS -------------
    private final ZombieAnimationSet animations;
    private final ZombieSoundSet sounds;

    private long chompId = -1;

    public NormalZombie() {
        super();

        this.health = MAX_HEALTH;
        this.speed = MOVE_SPEED;

        this.animations = new ZombieAnimationSet();
        this.sounds = new ZombieSoundSet();

        // First frame defines zombie size
        TextureRegion first = animations.walk.getKeyFrame(0f);
        setSize(first.getRegionWidth(), first.getRegionHeight());

        // ZOMBIE SPAWN SOUND
        sounds.playGroan();
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        stateTime += delta;

        switch (state) {

            case DYING:
                updateDying(delta);
                break;

            case BURNED:
                updateBurned(delta);
                break;

            case EAT:
                updateEating(delta);
                break;

            case WALK:
            default:
                updateWalking(delta);
                break;
        }
    }

    // ------------------ WALK LOGIC ------------------
    private void updateWalking(float delta) {
        if (isTouchingPlant()) {
            changeState(ZombieState.EAT);
            return;
        }

        update(delta); // movement from parent class
    }

    // ------------------ EAT LOGIC -------------------
    private void updateEating(float delta) {
        if (!isTouchingPlant()) {
            changeState(ZombieState.WALK);
            return;
        }
        // eating logic handled by animation + plant damage elsewhere
    }

    // ------------------ NORMAL DEATH ------------------
    private void updateDying(float delta) {
        stopChomp();

        if (animations.dying.isAnimationFinished(stateTime)) {
            super.die();
        }
    }

    // ------------------ CHERRY BOMB BURN DEATH ------------------
    private void updateBurned(float delta) {
        stopChomp();

        if (animations.burned.isAnimationFinished(stateTime)) {
            super.die(); // remove zombie completely
        }
    }

    // ------------------ STATE CHANGE ------------------
    private void changeState(ZombieState newState) {
        if (state == newState) return;

        // EXIT old state
        if (state == ZombieState.EAT)
            stopChomp();

        // ENTER new state
        state = newState;
        stateTime = 0f;

        if (state == ZombieState.EAT)
            startChomp();
    }

    private void startChomp() {
        if (chompId == -1) {
            chompId = sounds.startLoopChomp();
        }
    }

    private void stopChomp() {
        if (chompId != -1) {
            sounds.stopChomp(chompId);
            chompId = -1;
        }
    }

    // ------------------ DAMAGE HANDLING ------------------
    @Override
    public void takeDamage(int damage) {
        if (state == ZombieState.DYING || state == ZombieState.BURNED)
            return;

        health -= damage;

        if (health <= 0) {
            changeState(ZombieState.DYING);
        }
    }

    // SPECIAL DEATH FOR CHERRY BOMB
    public void burnToDeath() {
        if (state == ZombieState.BURNED)
            return;

        stopChomp();

        health = 0;
        state = ZombieState.BURNED;
        stateTime = 0f;
    }

    // ------------------ RENDER ------------------
    @Override
    public void draw(Batch batch, float parentAlpha) {

        Animation<TextureRegion> anim;

        switch (state) {

            case DYING:
                anim = animations.dying;
                break;

            case BURNED:
                anim = animations.burned;
                break;

            case EAT:
                anim = animations.eat;
                break;

            case WALK:
            default:
                anim = animations.walk;
                break;
        }

        TextureRegion frame = anim.getKeyFrame(stateTime);
        batch.draw(frame, getX(), getY(), getWidth(), getHeight());
    }

    @Override
    public void dispose() {
        animations.dispose();
        sounds.dispose();
    }

    // ======================= COMPONENT CLASSES =======================

    /**
     * Animation Holder (Single Responsibility)
     */
    private static class ZombieAnimationSet {

        final Animation<TextureRegion> walk;
        final Animation<TextureRegion> dying;
        final Animation<TextureRegion> eat;
        final Animation<TextureRegion> burned;

        final Texture walkTex;
        final Texture dieTex;
        final Texture eatTex;
        final Texture burnedTex;

        ZombieAnimationSet() {

            walkTex = new Texture("assets/images/Zombies/NormalZombieRun.gif");
            dieTex = new Texture("assets/images/Zombies/ZombieDie.gif");
            eatTex = new Texture("assets/images/Zombies/NormalZombieEat.gif");
            burnedTex = new Texture("assets/images/Zombies/BurntZombie.gif");

            walk = GifManager.createAnim(walkTex, 4, 0.20f, Animation.PlayMode.LOOP);
            dying = GifManager.createAnim(dieTex, 4, 0.20f, Animation.PlayMode.NORMAL);
            eat = GifManager.createAnim(eatTex, 4, 0.25f, Animation.PlayMode.LOOP);
            burned = GifManager.createAnim(burnedTex, 4, 0.15f, Animation.PlayMode.NORMAL);
        }

        void dispose() {
            walkTex.dispose();
            dieTex.dispose();
            eatTex.dispose();
            burnedTex.dispose();
        }
    }

    /**
     * Sound Holder (Single Responsibility)
     */
    private static class ZombieSoundSet {

        private final Sound chomp;
        private final Sound groan;

        ZombieSoundSet() {
            chomp = Gdx.audio.newSound(Gdx.files.internal("assets/sounds/chomp.wav"));
            groan = Gdx.audio.newSound(Gdx.files.internal("assets/sounds/groan.wav"));
        }

        long startLoopChomp() {
            return chomp.loop();
        }

        void stopChomp(long id) {
            chomp.stop(id);
        }

        void playGroan() {
            groan.play();
        }

        void dispose() {
            chomp.dispose();
            groan.dispose();
        }
    }

    /**
     * States following Open/Closed Principle
     */
    private enum ZombieState {
        WALK,
        EAT,
        DYING,
        BURNED   // <- CHERRY BOMB SPECIAL DEATH
    }
}
