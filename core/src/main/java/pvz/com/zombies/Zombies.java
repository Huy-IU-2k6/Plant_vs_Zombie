package pvz.com.zombies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;

public class Zombies extends Actor {

    protected int health;
    protected float speed;
    protected static boolean gameOver = false;
    protected static int zombieCount = 0;

    protected Sound comingZombieSound;

    public Zombies() {

        comingZombieSound = Gdx.audio.newSound(Gdx.files.internal("coming_zombie.wav"));

        if (zombieCount == 0) {
            comingZombieSound.play();
        }

        zombieCount++;
    }

    public void update(float delta) {

        if (!isTouchingPlant()) {
            moveBy(-speed * delta, 0);
        }

        checkGameOver();
    }

    public void takeDamage(int dmg) {
        health -= dmg;

        if (health <= 0) {
            die();
        }
    }

    protected void die() {

        Stage stage = getStage();
        if (stage != null) {
            stage.getRoot().removeActor(this);
        }
    }

    protected void checkGameOver() {
        if (getX() < 260) {
            gameOver = true;

        }
    }

    protected boolean isTouchingPlant() {

        return false;
    }
}
