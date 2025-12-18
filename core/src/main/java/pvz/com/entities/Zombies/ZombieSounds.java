package pvz.com.entities.Zombies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils; // Nhớ import MathUtils

public class ZombieSounds {
    // Assets tĩnh (Load 1 lần dùng chung)
    private static Sound GROAN, CHOMP;
    private static boolean loaded = false;

    private float timer = 0f;
    private float chompTimer = 0f;

    public ZombieSounds(BaseZombie zombie) {
        // [QUAN TRỌNG] Random thời gian bắt đầu để các zombie không kêu cùng lúc
        this.timer = MathUtils.random(0f, 3.0f);

        if (!loaded) {
            try {
                // Kiểm tra kỹ đường dẫn: assets/sounds/groan.wav
                GROAN = Gdx.audio.newSound(Gdx.files.internal("sounds/groan.wav"));
                CHOMP = Gdx.audio.newSound(Gdx.files.internal("sounds/chomp.wav"));
                
                // Tiếng coming (Load thử, nếu lỗi thì bỏ qua)
                try {
                    Gdx.audio.newSound(Gdx.files.internal("sounds/zombies_are_coming.wav"));
                } catch (Exception ex) {
                    System.out.println("Warning: Không thấy file zombies_are_coming.wav");
                }

                loaded = true;
                System.out.println(">>> ZombieSounds: LOADED OK!"); 
            } catch (Exception e) {
                // [SỬA LỖI] Phải in lỗi ra mới biết file nào thiếu
                System.err.println("!!! LỖI LOAD SOUND ZOMBIE: " + e.getMessage());
                e.printStackTrace(); 
            }
        }
    }

    public void act(float delta, boolean isEating) {
        if (!loaded) return;

        timer += delta;
        // 4 giây random rên 1 lần
        if (timer > 4f) {
            // [FIX] Kiểm tra null trước khi play để không crash game
            if (GROAN != null && MathUtils.randomBoolean(0.4f)) { // 40% tỉ lệ kêu
                long id = GROAN.play(0.5f);
                // Chỉnh méo tiếng tí xíu cho tự nhiên
                GROAN.setPitch(id, MathUtils.random(0.9f, 1.1f));
            }
            // Reset về số âm ngẫu nhiên để lệch nhịp
            timer = -MathUtils.random(0f, 2.0f);
        }

        // Nếu đang ăn thì phát tiếng nhai
        if (isEating) {
            chompTimer += delta;
            if (chompTimer > 0.6f) {
                if (CHOMP != null) CHOMP.play(0.5f);
                chompTimer = 0f;
            }
        } else {
            chompTimer = 0f;
        }
    }

    public void dispose() {} 
    
    public static void disposeAll() {
        if (GROAN != null) GROAN.dispose();
        if (CHOMP != null) CHOMP.dispose();
        GROAN = null;
        CHOMP = null;
        loaded = false;
    }
}