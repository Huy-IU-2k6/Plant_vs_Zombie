package pvz.com.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;

public class SoundManager implements Disposable {

    private static SoundManager instance;

    public static SoundManager i() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }

    // Lưu nhạc nền & hiệu ứng
    private final ObjectMap<String, Music> musics = new ObjectMap<>();
    private final ObjectMap<String, Sound> sounds = new ObjectMap<>();

    private Music currentMusic;
    private float musicVolume = 1f;
    private float sfxVolume = 1f;

    private SoundManager() {
        // TODO: chỉnh lại path cho đúng với project của bạn

        // Background Music
        // musics.put("menu",
        // Gdx.audio.newMusic(Gdx.files.internal("sounds/menu_bgm.mp3")));
        musics.put("menu",
                Gdx.audio.newMusic(Gdx.files.internal("musics/Grasswalk.mp3")));

        // Sound Effect
        // sounds.put("chomp",
        // Gdx.audio.newSound(Gdx.files.internal("sounds/chomp.wav")));
        sounds.put("menu_click", Gdx.audio.newSound(Gdx.files.internal("sounds/menu_click.mp3")));
    }

    // ===== MUSIC =====
    public void playMusic(String id, boolean loop) {
        Music music = musics.get(id);
        if (music == null)
            return;

        // stop nhạc đang chạy
        if (currentMusic != null && currentMusic.isPlaying()) {
            currentMusic.stop();
        }

        currentMusic = music;
        currentMusic.setLooping(loop);
        currentMusic.setVolume(musicVolume);
        currentMusic.play();
    }

    public void stopMusic() {
        if (currentMusic != null) {
            currentMusic.stop();
        }
    }

    public void setMusicVolume(float volume) {
        this.musicVolume = volume;
        if (currentMusic != null) {
            currentMusic.setVolume(musicVolume);
        }
    }

    // ===== SFX =====
    public void playSound(String id) {
        Sound sound = sounds.get(id);
        if (sound == null)
            return;
        sound.play(sfxVolume);
    }

    public void setSfxVolume(float volume) {
        this.sfxVolume = volume;
    }

    // ===== LIFECYCLE =====
    @Override
    public void dispose() {
        for (Music m : musics.values()) {
            m.dispose();
        }
        for (Sound s : sounds.values()) {
            s.dispose();
        }
    }
}
