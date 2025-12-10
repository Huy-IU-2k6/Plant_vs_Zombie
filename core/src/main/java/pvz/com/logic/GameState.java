package pvz.com.logic;

/**
 * Trạng thái tổng của màn chơi.
 * Dùng chung cho GameScreen, GameWorld, Zombies, v.v...
 */
public final class GameState {

    public enum State {
        COUNTDOWN, // đang đếm ngược
        PLAYING, // đang chơi bình thường
        GAME_OVER // kết thúc (thua hoặc thắng)
    }

    private State state = State.COUNTDOWN;

    // true nếu người chơi thắng, false nếu thua
    private boolean playerWon = false;

    public GameState() {
    }

    // ===== STATE =====

    public State getState() {
        return state;
    }

    public void setState(State newState) {
        this.state = newState;
    }

    public boolean isCountdown() {
        return state == State.COUNTDOWN;
    }

    public boolean isPlaying() {
        return state == State.PLAYING;
    }

    public boolean isGameOver() {
        return state == State.GAME_OVER;
    }

    // ===== GAME OVER =====

    /**
     * Gọi khi game kết thúc.
     *
     * @param playerWon true nếu người chơi thắng, false nếu thua
     */
    public void setGameOver(boolean playerWon) {
        this.state = State.GAME_OVER;
        this.playerWon = playerWon;
    }

    public boolean isPlayerWon() {
        return playerWon;
    }
}
