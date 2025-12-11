package pvz.com.entities.Zombies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;

public class ZombieSounds {
    // ===== TIMERS =====
    private float soundTimer = 0f;
    private float chompTimer = 0f;

    // ===== SOUNDS (STATIC - Dùng chung cho cả bầy) =====
    // Chỉ nạp 1 lần vào bộ nhớ
    public static final Sound comingZombieSound = Gdx.audio.newSound(
            Gdx.files.internal("sounds/zombies_are_coming.wav"));
    public static final Sound groanSound = Gdx.audio.newSound(
            Gdx.files.internal("sounds/groan.wav"));
    public static final Sound brainzSound = Gdx.audio.newSound(
            Gdx.files.internal("sounds/brainz.wav"));
    public static final Sound chompSound = Gdx.audio.newSound(
            Gdx.files.internal("sounds/chomp.wav"));

    private Zombies zombies;

    public ZombieSounds(Zombies zombies) {
        // Tiếng khi mới xuất hiện
        // Kiểm tra null để tránh lỗi nếu file âm thanh chưa nạp được
        if (comingZombieSound != null) comingZombieSound.play(0.8f);
        if (groanSound != null) groanSound.play(0.6f);

        if (Math.random() < 0.3f && brainzSound != null) {
            brainzSound.play(0.7f);
        }
        this.zombies = zombies;
    }

    public void act(float delta) {
        // Random tiếng rên (groan/brainz)
        soundTimer += delta;
        if (soundTimer > 4f) {
            double r = Math.random();
            if (r < 0.7) {
                if (groanSound != null) groanSound.play(0.5f);
            } else {
                if (brainzSound != null) brainzSound.play(0.6f);
            }
            soundTimer = 0f;
        }

        // Tiếng cắn khi đang ăn
        if (zombies.isEating()) {
            chompTimer += delta;
            if (chompTimer > 0.85f) {
                if (chompSound != null) chompSound.play(0.7f);
                chompTimer = 0f;
            }
        } else {
            chompTimer = 0f;
        }
    }

    /**
     * Hàm này được gọi khi MỘT con zombie chết.
     * TUYỆT ĐỐI KHÔNG dispose âm thanh static ở đây.
     */
    public void dispose() {
        // Chỉ hủy tham chiếu, không xóa Sound vì Sound đang dùng chung
        zombies = null;
    }

    /**
     * Hàm này được gọi khi thoát GameScreen (Game Over hoặc tắt game).
     * Lúc này mới xóa sạch âm thanh khỏi RAM.
     */
    public static void disposeAll() {
        if (comingZombieSound != null) comingZombieSound.dispose();
        if (groanSound != null) groanSound.dispose();
        if (brainzSound != null) brainzSound.dispose();
        if (chompSound != null) chompSound.dispose();
    }
}