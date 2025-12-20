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

        SUNFLOWER_IDLE = load("images/Plants/SunFlower/SunFlower_", 18, PLANT_FRAME_DURATION, Animation.PlayMode.LOOP);


        PEASHOOTER_IDLE = load("images/Plants/Peashooter/Peashooter_", 13, PLANT_FRAME_DURATION, Animation.PlayMode.LOOP);
        SNOWPEA_IDLE = load("images/Plants/SnowPea/SnowPea_", 15, PLANT_FRAME_DURATION, Animation.PlayMode.LOOP);
        REPEATER_IDLE = load("images/Plants/repeater/repeater_", 42, PLANT_FRAME_DURATION, Animation.PlayMode.LOOP);


        WALLNUT_FULL = load("images/Plants/WallNut/WallNut/WallNut_", 10, PLANT_FRAME_DURATION, Animation.PlayMode.LOOP);
        WALLNUT_CRACKED1 = load("images/Plants/WallNut/WallNut_cracked1/WallNut_cracked1_", 10, PLANT_FRAME_DURATION,
                Animation.PlayMode.LOOP);
        WALLNUT_CRACKED2 = load("images/Plants/WallNut/WallNut_cracked2/WallNut_cracked2_", 10, PLANT_FRAME_DURATION,
                Animation.PlayMode.LOOP);


        POTATO_GROWING = load("images/Plants/PotatoMine/planted/planted_", 29, 0.06f, Animation.PlayMode.NORMAL);
        POTATO_UNARMED = load("images/Plants/PotatoMine/init/init_", 1, 0.06f, Animation.PlayMode.LOOP);
        POTATO_RISING = load("images/Plants/PotatoMine/grow/grow_", 25, 0.06f, Animation.PlayMode.NORMAL);
        POTATO_IDLE = load("images/Plants/PotatoMine/Idle/idle_", 30, 0.06f, Animation.PlayMode.LOOP);
        POTATO_EXPLODE = load("images/Plants/PotatoMine/explode/explode_", 26, 0.06f, Animation.PlayMode.NORMAL);


        CHERRY_IDLE = load("images/Plants/CherryBomb/CherryBomb/CherryBomb_", 6, PLANT_FRAME_DURATION, Animation.PlayMode.LOOP);
        CHERRY_EXPLODE = load("images/Plants/CherryBomb/powie/powie_", 28, 18, 0.1f, Animation.PlayMode.NORMAL);
        System.out.println(">>> Plant Assets Loaded Successfully!");
    }


    private static Animation<TextureRegion> load(String prefix, int count, float duration, Animation.PlayMode mode) {
        return load(prefix, count, 0, duration, mode);
    }


    private static Animation<TextureRegion> load(String prefix, int endIdx, int startIdx, float duration,
            Animation.PlayMode mode) {
        Array<TextureRegion> frames = new Array<>();
        for (int i = startIdx; i <= endIdx; i++) {
            try {
                Texture tex = new Texture(prefix + i + ".png");
                tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);


                textures.add(tex);

                frames.add(new TextureRegion(tex));
            } catch (Exception e) {


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
