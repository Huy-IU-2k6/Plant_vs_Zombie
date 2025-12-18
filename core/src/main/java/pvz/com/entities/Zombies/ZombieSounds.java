package pvz.com.entities.Zombies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;

public class ZombieSounds {
    private float soundTimer = 0f;
    private float chompTimer = 0f;

    
    public static Sound comingZombieSound;
    public static Sound groanSound;
    public static Sound brainzSound;
    public static Sound chompSound;

    private Zombies zombies;

    public static void loadSounds() {
        
        if (comingZombieSound == null) {
            comingZombieSound = Gdx.audio.newSound(Gdx.files.internal("sounds/zombies_are_coming.wav"));
            groanSound = Gdx.audio.newSound(Gdx.files.internal("sounds/groan.wav"));
            brainzSound = Gdx.audio.newSound(Gdx.files.internal("sounds/brainz.wav"));
            chompSound = Gdx.audio.newSound(Gdx.files.internal("sounds/chomp.wav"));
        }
    }

    public ZombieSounds(Zombies zombies) {
        loadSounds(); 
        if (comingZombieSound != null) comingZombieSound.play(0.8f);
        if (groanSound != null) groanSound.play(0.6f);
        if (Math.random() < 0.3f && brainzSound != null) {
            brainzSound.play(0.7f);
        }
        this.zombies = zombies;
    }

    

    public static void disposeAll() {
        if (comingZombieSound != null) comingZombieSound.dispose();
        if (groanSound != null) groanSound.dispose();
        if (brainzSound != null) brainzSound.dispose();
        if (chompSound != null) chompSound.dispose();
        
        
        comingZombieSound = null;
        groanSound = null;
        brainzSound = null;
        chompSound = null;
    }
}