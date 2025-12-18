package pvz.com.factories;

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

    // Texture dự phòng (Hộp trắng)
    private static Texture debugTexture;

    public static void loadAll() {
        // Tạo texture trắng dự phòng (1x1 pixel)
        createDebugTexture();

        // Load ảnh thật (Nếu sai đường dẫn sẽ dùng ảnh trắng)
        NORMAL_WALK = load("images/Zombies/NormalZombie/Zombie/Zombie_", 21, 0.055f, Animation.PlayMode.LOOP);
        NORMAL_EAT  = load("images/Zombies/NormalZombie/ZombieAttack/ZombieAttack_", 10, 0.08f, Animation.PlayMode.LOOP);
        NORMAL_DIE  = load("images/Zombies/NormalZombie/ZombieDie/ZombieDie_", 9, 0.08f, Animation.PlayMode.NORMAL);
        HEAD_POP    = load("images/Zombies/NormalZombie/ZombieHead/ZombieHead_", 10, 0.08f, Animation.PlayMode.NORMAL);
        CHARRED     = load("images/Zombies/NormalZombie/BoomDie/BoomDie_", 19, 0.08f, Animation.PlayMode.NORMAL);
        
        CONE_WALK   = load("images/Zombies/ConeheadZombie/Zombie/Zombie_", 21, 0.055f, Animation.PlayMode.LOOP);
        CONE_EAT    = load("images/Zombies/ConeheadZombie/ZombieAttack/ZombieAttack_", 10, 0.08f, Animation.PlayMode.LOOP);
        
        // Dùng tạm ảnh Cone cho Bucket để test (tránh tàng hình nếu chưa có file Bucket)
        BUCKET_WALK = loadFallback(CONE_WALK, "images/Zombies/BucketheadZombie/Zombie/Zombie_", 14, 0.055f);
        BUCKET_EAT  = loadFallback(CONE_EAT, "images/Zombies/BucketheadZombie/ZombieAttack/ZombieAttack_", 10, 0.08f);
        
        // Dùng tạm ảnh Normal cho Charge
        CHARGE_WALK = loadFallback(NORMAL_WALK, "images/Zombies/ChargeZombie/Zombie/Zombie_", 21, 0.055f);
        CHARGE_EAT  = loadFallback(NORMAL_EAT, "images/Zombies/ChargeZombie/ZombieAttack/ZombieAttack_", 10, 0.08f);
        
        System.out.println(">>> Zombie Assets Loaded!");
    }

    private static Animation<TextureRegion> load(String prefix, int count, float speed, Animation.PlayMode mode) {
        Array<TextureRegion> frames = new Array<>();
        for (int i = 0; i <= count; i++) {
            try {
                // Kiểm tra đường dẫn
                String path = prefix + i + ".png";
                Texture t = new Texture(path);
                t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
                frames.add(new TextureRegion(t));
            } catch (Exception e) { 
                // Không tìm thấy file này, bỏ qua
            }
        }
        
        // [QUAN TRỌNG] Nếu không tìm thấy bất kỳ ảnh nào -> Dùng Hộp Trắng
        if (frames.size == 0) {
            System.err.println("!!! MISSING TEXTURE: " + prefix + " -> Using Debug Box");
            frames.add(new TextureRegion(debugTexture));
        }

        Animation<TextureRegion> anim = new Animation<>(speed, frames);
        anim.setPlayMode(mode);
        return anim;
    }

    private static Animation<TextureRegion> loadFallback(Animation<TextureRegion> fallback, String prefix, int count, float speed) {
        Animation<TextureRegion> anim = load(prefix, count, speed, Animation.PlayMode.LOOP);
        // Nếu load ra hộp trắng (size=1 và là debugTexture) thì dùng fallback
        if (anim.getKeyFrames()[0].getTexture() == debugTexture) {
            return fallback;
        }
        return anim;
    }
    
    private static void createDebugTexture() {
        if (debugTexture == null) {
            Pixmap pixmap = new Pixmap(64, 64, Pixmap.Format.RGBA8888);
            pixmap.setColor(Color.WHITE);
            pixmap.fill(); // Tô trắng toàn bộ
            // Vẽ viền đỏ để dễ nhìn
            pixmap.setColor(Color.RED);
            pixmap.drawRectangle(0, 0, 64, 64);
            debugTexture = new Texture(pixmap);
            pixmap.dispose();
        }
    }

    public static void dispose() {
        if (debugTexture != null) debugTexture.dispose();
        // Dispose các texture đã load khác nếu cần
    }
}