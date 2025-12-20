package pvz.com.entities.factories;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

public class ZombieAssetLoader {

    public static Animation<TextureRegion> NORMAL_WALK, NORMAL_EAT, NORMAL_DIE, HEAD_POP, CHARRED;
    public static Animation<TextureRegion> CONE_WALK, CONE_EAT;
    public static Animation<TextureRegion> BUCKET_WALK, BUCKET_EAT;
    public static Animation<TextureRegion> CHARGE_WALK, CHARGE_EAT;

    private static Texture debugTexture;

    public static void loadAll() {

        createDebugTexture();

        // TODO: add zombie lost head state
        // TODO: add zombie lost head attack state

        NORMAL_DIE = load("images/characters/zombies/zombie/normal_die_state/normal_die_state_", 9, 0.08f,
                Animation.PlayMode.NORMAL);
        HEAD_POP = load("images/characters/zombies/zombie/head_falling_state/head_falling_state_", 10, 0.08f,
                Animation.PlayMode.NORMAL);

        CHARRED = load("images/characters/zombies/zombie/boom_die_state/boom_die_state_", 19, 0.08f,
                Animation.PlayMode.NORMAL);

        NORMAL_WALK = load("images/characters/zombies/normal_zombie/normal_state/normal_state_", 21, 0.055f,
                Animation.PlayMode.LOOP);
        NORMAL_EAT = load("images/characters/zombies/normal_zombie/attack_state/attack_state_", 10, 0.08f,
                Animation.PlayMode.LOOP);

        CONE_WALK = load("images/characters/zombies/conehead_zombie/normal_state/normal_state_", 21, 0.055f,
                Animation.PlayMode.LOOP);
        CONE_EAT = load("images/characters/zombies/conehead_zombie/attack_state/attack_state_", 11, 0.08f,
                Animation.PlayMode.LOOP);

        BUCKET_WALK = loadFallback(CONE_WALK, "images/characters/zombies/buckethead_zombie/normal_state/normal_state_",
                14,
                0.055f);
        BUCKET_EAT = loadFallback(CONE_EAT, "images/characters/zombies/buckethead_zombie/attack_state/attack_state_",
                10,
                0.08f);

        CHARGE_WALK = loadFallback(NORMAL_WALK, "images/characters/zombies/charge_zombie/normal_state/normal_state_",
                59,
                0.02f);
        CHARGE_EAT = loadFallback(NORMAL_EAT, "images/characters/zombies/charge_zombie/attack_state/attack_state_", 60,
                0.02f);

        System.out.println(">>> Zombie Assets Loaded!");
    }

    private static Animation<TextureRegion> load(String prefix, int count, float speed, Animation.PlayMode mode) {
        Array<TextureRegion> frames = new Array<>();
        for (int i = 0; i <= count; i++) {
            try {

                String path = prefix + i + ".png";
                Texture t = new Texture(path);
                t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
                frames.add(new TextureRegion(t));
            } catch (Exception e) {

            }
        }

        if (frames.size == 0) {
            System.err.println("!!! MISSING TEXTURE: " + prefix + " -> Using Debug Box");
            frames.add(new TextureRegion(debugTexture));
        }

        Animation<TextureRegion> anim = new Animation<>(speed, frames);
        anim.setPlayMode(mode);
        return anim;
    }

    private static Animation<TextureRegion> loadFallback(Animation<TextureRegion> fallback, String prefix, int count,
            float speed) {
        Animation<TextureRegion> anim = load(prefix, count, speed, Animation.PlayMode.LOOP);

        if (anim.getKeyFrame(0).getTexture() == debugTexture) {
            return fallback;
        }
        return anim;
    }

    private static void createDebugTexture() {
        if (debugTexture == null) {
            Pixmap pixmap = new Pixmap(64, 64, Pixmap.Format.RGBA8888);
            pixmap.setColor(Color.WHITE);
            pixmap.fill();

            pixmap.setColor(Color.RED);
            pixmap.drawRectangle(0, 0, 64, 64);
            debugTexture = new Texture(pixmap);
            pixmap.dispose();
        }
    }

    public static void dispose() {
        if (debugTexture != null)
            debugTexture.dispose();

    }
}
