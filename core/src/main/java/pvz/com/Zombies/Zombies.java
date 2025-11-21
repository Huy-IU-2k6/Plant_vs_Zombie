package pvz.com.Zombies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class Zombies extends Actor {

    // ----- STATIC STATE -----
    protected static boolean gameOver = false;
    protected static int zombieCount = 0;
    private static final Sound comingZombieSound = Gdx.audio
            .newSound(Gdx.files.internal("assets/sounds/zombies_are_coming.wav"));

    // ----- INSTANCE STATE -----
    protected int health;
    protected float speed;

    public Zombies() {
        // Chỉ phát sound khi con zombie đầu tiên xuất hiện
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
        // Actor đã có sẵn remove() để tự xoá khỏi Stage
        remove();
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
