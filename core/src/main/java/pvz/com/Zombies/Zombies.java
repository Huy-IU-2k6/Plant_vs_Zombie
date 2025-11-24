package pvz.com.Zombies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Rectangle; // <--- thêm dòng này
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

    // đánh dấu đã chết chưa (để không xử lý lại)
    protected boolean dead = false;

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
        if (dead)
            return;
        health -= dmg;
        if (health <= 0) {
            die();
        }
    }

    // Cho mấy thằng khác gọi thẳng
    public void kill() {
        if (!dead) {
            die();
        }
    }

    protected void die() {
        dead = true;

        // giảm số lượng zombie đang sống (phòng dư thôi)
        if (zombieCount > 0) {
            zombieCount--;
        }

        // Actor đã có sẵn remove() để tự xoá khỏi Stage
        remove();
    }

    protected void checkGameOver() {
        if (!dead && getX() < 260) {
            gameOver = true;
        }
    }

    protected boolean isTouchingPlant() {
        return false;
    }

    // Cho lawn mower / va chạm khác dùng
    public Rectangle getBounds() {
        return new Rectangle(getX(), getY(), getWidth(), getHeight());
    }

    public boolean isDead() {
        return dead;
    }
}
