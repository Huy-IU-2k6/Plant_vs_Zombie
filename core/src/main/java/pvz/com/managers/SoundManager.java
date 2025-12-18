package pvz.com.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

public class SoundManager {

    // Sound Effects
    public static Sound GROAN;
    public static Sound CHOMP;
    public static Sound COMING; // Tiếng "The zombies are coming"

    // Music (Nhạc nền)
    public static Music musicBg;

    public static void loadAll() {
        try {
            // 1. Load Sound Effects (Dùng .wav như file cũ của bạn)
            GROAN  = Gdx.audio.newSound(Gdx.files.internal("sounds/groan.wav"));
            CHOMP  = Gdx.audio.newSound(Gdx.files.internal("sounds/chomp.wav"));
            COMING = Gdx.audio.newSound(Gdx.files.internal("sounds/zombies_are_coming.wav"));

            // 2. Load Music (Nhạc nền thường là .mp3 hoặc .ogg cho nhẹ)
            // Nếu bạn chưa có file này, hãy chép file nhạc vào assets/musics/
            musicBg = Gdx.audio.newMusic(Gdx.files.internal("musics/Grasswalk.mp3")); 
            
            System.out.println(">>> SoundManager: Audio loaded successfully!");
        } catch (Exception e) {
            // In lỗi ra để biết đường sửa (quan trọng!)
            System.err.println("!!! LỖI LOAD SOUND: " + e.getMessage());
        }
    }

    // Các hàm tiện ích để gọi phát tiếng
    public static void playGroan() {
        if (GROAN != null) GROAN.play(0.5f); // Âm lượng 50%
    }

    public static void playChomp() {
        if (CHOMP != null) CHOMP.play(0.5f);
    }
    
    public static void playComing() {
        if (COMING != null) COMING.play(1.0f);
    }

    public static void playMusic() {
        if (musicBg != null && !musicBg.isPlaying()) {
            musicBg.setLooping(true);
            musicBg.setVolume(0.8f);
            musicBg.play();
        }
    }

    public static void dispose() {
        if (GROAN != null) GROAN.dispose();
        if (CHOMP != null) CHOMP.dispose();
        if (COMING != null) COMING.dispose();
        if (musicBg != null) musicBg.dispose();
    }
}