package pvz.com.entities.zombies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils;

public class ZombieSounds {

    private static Sound GROAN, CHOMP;
    private static boolean loaded = false;

    private float timer = 0f;
    private float chompTimer = 0f;

    public ZombieSounds(BaseZombie zombie) {

        this.timer = MathUtils.random(0f, 3.0f);

        if (!loaded) {
            try {

                GROAN = Gdx.audio.newSound(Gdx.files.internal("sounds/groan.wav"));
                CHOMP = Gdx.audio.newSound(Gdx.files.internal("sounds/chomp.wav"));
                

                try {
                    Gdx.audio.newSound(Gdx.files.internal("sounds/zombies_are_coming.wav"));
                } catch (Exception ex) {
                    System.out.println("Warning: Không thấy file zombies_are_coming.wav");
                }

                loaded = true;
                System.out.println(">>> ZombieSounds: LOADED OK!"); 
            } catch (Exception e) {

                System.err.println("!!! LỖI LOAD SOUND ZOMBIE: " + e.getMessage());
                e.printStackTrace(); 
            }
        }
    }

    public void act(float delta, boolean isEating) {
        if (!loaded) return;

        timer += delta;

        if (timer > 4f) {

            if (GROAN != null && MathUtils.randomBoolean(0.4f)) {
                long id = GROAN.play(0.5f);

                GROAN.setPitch(id, MathUtils.random(0.9f, 1.1f));
            }

            timer = -MathUtils.random(0f, 2.0f);
        }


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
