package pvz.com.entities.Zombies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;

public class ZombieSounds {
    // Assets tĩnh (Load 1 lần dùng chung)
    private static Sound GROAN, CHOMP;
    private static boolean loaded = false;

    private float timer = 0f;
    private float chompTimer = 0f;

    // [FIX] Constructor nhận BaseZombie (hoặc null cũng được vì class này đã refactor)
    public ZombieSounds(BaseZombie zombie) {
        if (!loaded) {
            try {
                GROAN = Gdx.audio.newSound(Gdx.files.internal("sounds/groan.wav"));
                CHOMP = Gdx.audio.newSound(Gdx.files.internal("sounds/chomp.wav"));
                
                // Play tiếng zombie xuất hiện (chỉ xác suất nhỏ để đỡ ồn)
                if (Gdx.audio.newSound(Gdx.files.internal("sounds/zombies_are_coming.wav")) != null) {
                     // Logic play sound coming...
                }
                loaded = true;
            } catch (Exception e) { /* Ignore */ }
        }
    }

    public void act(float delta, boolean isEating) {
        if (!loaded) return;

        timer += delta;
        // 4 giây random rên 1 lần
        if (timer > 4f) {
            if (Math.random() < 0.4) GROAN.play(0.5f);
            timer = 0f;
        }

        // Nếu đang ăn thì phát tiếng nhai
        if (isEating) {
            chompTimer += delta;
            if (chompTimer > 0.6f) {
                CHOMP.play(0.5f);
                chompTimer = 0f;
            }
        }
    }

    public void dispose() {} // Không cần dispose instance
    
    public static void disposeAll() {
        if (GROAN != null) GROAN.dispose();
        if (CHOMP != null) CHOMP.dispose();
        loaded = false;
    }
}