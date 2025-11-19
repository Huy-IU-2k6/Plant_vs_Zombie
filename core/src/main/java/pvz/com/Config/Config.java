package ;

import java.util.Random;

// Singleton class
public class Config {
    // Variables
    private static Random random = null;

    // Getters
    public static Random getRandom() {
        if (random == null) {
            random = new Random();
        }
        return random;
    }
}