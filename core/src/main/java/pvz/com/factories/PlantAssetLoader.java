package pvz.com.factories;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

import java.util.ArrayList;
import java.util.List;

import pvz.com.managers.DesignConfig;

public class PlantAssetLoader {

    public static final float PLANT_FRAME_DURATION = DesignConfig.PLANT_FRAME_DURATION;

    public static Animation<TextureRegion> SUNFLOWER_IDLE;
    public static Animation<TextureRegion> PEASHOOTER_IDLE;
    public static Animation<TextureRegion> SNOWPEA_IDLE;
    public static Animation<TextureRegion> REPEATER_IDLE;

    public static Animation<TextureRegion> WALLNUT_FULL;
    public static Animation<TextureRegion> WALLNUT_CRACKED1;
    public static Animation<TextureRegion> WALLNUT_CRACKED2;

    public static Animation<TextureRegion> POTATO_GROWING;
    public static Animation<TextureRegion> POTATO_UNARMED;
    public static Animation<TextureRegion> POTATO_RISING;
    public static Animation<TextureRegion> POTATO_IDLE;
    public static Animation<TextureRegion> POTATO_EXPLODE;

    public static Animation<TextureRegion> CHERRY_IDLE;
    public static Animation<TextureRegion> CHERRY_EXPLODE;

    private static final List<Texture> textures = new ArrayList<>();

    public static void loadAll() {

        SUNFLOWER_IDLE = load("images/characters/plants/sunflower_plant/normal_state/normal_state_", 18,
                PLANT_FRAME_DURATION,
                Animation.PlayMode.LOOP);

        PEASHOOTER_IDLE = load("images/characters/plants/peashooter_plant/normal_state/normal_state_", 13,
                PLANT_FRAME_DURATION,
                Animation.PlayMode.LOOP);
        SNOWPEA_IDLE = load("images/characters/plants/snowpea_plant/normal_state/normal_state_", 15,
                PLANT_FRAME_DURATION,
                Animation.PlayMode.LOOP);
        REPEATER_IDLE = load("images/characters/plants/repeater_plant/normal_state/normal_state_", 42,
                PLANT_FRAME_DURATION,
                Animation.PlayMode.LOOP);

        WALLNUT_FULL = load("images/characters/plants/wallnut_plant/normal_state/normal_state_", 10,
                PLANT_FRAME_DURATION,
                Animation.PlayMode.LOOP);
        WALLNUT_CRACKED1 = load("images/characters/plants/wallnut_plant/cracked1_state/cracked1_state_", 10,
                PLANT_FRAME_DURATION,
                Animation.PlayMode.LOOP);
        WALLNUT_CRACKED2 = load("images/characters/plants/wallnut_plant/cracked2_state/cracked2_state_", 10,
                PLANT_FRAME_DURATION,
                Animation.PlayMode.LOOP);

        POTATO_GROWING = load("images/characters/plants/potatomine_plant/planted_state/planted_state_", 29, PLANT_FRAME_DURATION,
                Animation.PlayMode.NORMAL);
        POTATO_UNARMED = load("images/characters/plants/potatomine_plant/init_state/init_state_", 1, PLANT_FRAME_DURATION,
                Animation.PlayMode.LOOP);
        POTATO_RISING = load("images/characters/plants/potatomine_plant/grow_state/grow_state_", 25, PLANT_FRAME_DURATION,
                Animation.PlayMode.NORMAL);
        POTATO_IDLE = load("images/characters/plants/potatomine_plant/idle_state/idle_state_", 30, PLANT_FRAME_DURATION,
                Animation.PlayMode.LOOP);
        POTATO_EXPLODE = load("images/characters/plants/potatomine_plant/explode_state/explode_state_", 26, PLANT_FRAME_DURATION,
                Animation.PlayMode.NORMAL);
        // TODO: Planted state for Potato Mine

        CHERRY_IDLE = load("images/characters/plants/cherrybomb_plant/normal_state/normal_state_", 6,
                PLANT_FRAME_DURATION,
                Animation.PlayMode.LOOP);
        CHERRY_EXPLODE = load("images/characters/plants/cherrybomb_plant/powie_state/powie_state_", 10,
                PLANT_FRAME_DURATION,
                Animation.PlayMode.NORMAL);

        System.out.println(">>> Plant Assets Loaded Successfully!");
    }

    private static Animation<TextureRegion> load(
            String prefix, int endIdx, float duration, Animation.PlayMode mode) {
        Array<TextureRegion> frames = new Array<>();

        for (int i = 0; i <= endIdx; i++) { // startIdx mặc định = 0
            try {
                Texture tex = new Texture(prefix + i + ".png");
                tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

                textures.add(tex);
                frames.add(new TextureRegion(tex));
            } catch (Exception ignored) {
                // bỏ qua file thiếu/lỗi load
            }
        }

        if (frames.size == 0) {
            return new Animation<>(duration, new Array<>(), mode);
        }

        return new Animation<>(duration, frames, mode);
    }

    public static void dispose() {
        for (Texture tex : textures) {
            if (tex != null)
                tex.dispose();
        }
        textures.clear();
        System.out.println(">>> Plant Assets Disposed!");
    }
}
