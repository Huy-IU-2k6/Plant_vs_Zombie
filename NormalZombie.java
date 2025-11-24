package pvz.com.zombies;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class NormalZombie extends Zombies {

    private Animation<TextureRegion> walkAnim;
    private Animation<TextureRegion> eatAnim;
    private Animation<TextureRegion> dieAnim;

    private TextureRegion currentFrame;

    private float stateTime = 0f;

    private enum State { WALK, EAT, DIE }
    private State currentState = State.WALK;

    public NormalZombie() {

        // Zombie values
        this.health = 100;
        this.speed = 40f;   // walking speed (positive so parent moves left)

        // Load animations
        walkAnim = loadAnimation("NormalZombieRun/", 0.12f);
        eatAnim  = loadAnimation("NormalZombieEat/", 0.12f);
        dieAnim  = loadAnimation("ZombieDie/", 0.12f);

        // set actor size
        setBounds(getX(), getY(),
                  walkAnim.getKeyFrame(0).getRegionWidth(),
                  walkAnim.getKeyFrame(0).getRegionHeight());
    }


    // ---------------------------
    // LOAD ANIMATION FROM FOLDER
    // ---------------------------
    private Animation<TextureRegion> loadAnimation(String folder, float speed) {
        java.io.File dir = new java.io.File(Gdx.files.internal(folder).file());
        java.io.File[] files = dir.listFiles();

        TextureRegion[] frames = new TextureRegion[files.length];

        for (int i = 0; i < files.length; i++) {
            frames[i] = new TextureRegion(new Texture(folder + files[i].getName()));
        }

        return new Animation<>(speed, frames);
    }


    // ---------------------------
    // MAIN UPDATE (CALLED BY Stage)
    // ---------------------------
    @Override
    public void act(float delta) {
        stateTime += delta;

        switch (currentState) {

            case WALK:
                super.update(delta); // use parent movement logic
                currentFrame = walkAnim.getKeyFrame(stateTime, true);

                if (isTouchingPlant()) {
                    changeState(State.EAT);
                }
                break;


            case EAT:
                currentFrame = eatAnim.getKeyFrame(stateTime, true);
                // Eating logic here (e.g. damage plant)
                if (!isTouchingPlant()) {
                    changeState(State.WALK);
                }
                break;


            case DIE:
                currentFrame = dieAnim.getKeyFrame(stateTime, false);

                if (dieAnim.isAnimationFinished(stateTime)) {
                    super.die(); // remove zombie from stage
                }
                break;
        }

        super.checkGameOver();
    }


    // ---------------------------
    // DAMAGE FROM BULLET
    // ---------------------------
    @Override
    public void takeDamage(int dmg) {
        if (currentState == State.DIE) return;

        health -= dmg;

        if (health <= 0) {
            changeState(State.DIE);
            return;
        }
    }


    // ---------------------------
    // CHANGE ANIMATION STATE
    // ---------------------------
    private void changeState(State s) {
        currentState = s;
        stateTime = 0; // restart animation
    }


    // ---------------------------
    // DRAW ZOMBIE
    // ---------------------------
    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.draw(currentFrame, getX(), getY());
    }
}
