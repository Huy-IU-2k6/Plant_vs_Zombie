package pvz.com.logic;


public final class GameState {

    public enum State {
        COUNTDOWN, 
        PLAYING, 
        GAME_OVER 
    }

    private State state = State.COUNTDOWN;

    
    private boolean playerWon = false;

    public GameState() {
    }

    

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

    
    public void setGameOver(boolean playerWon) {
        this.state = State.GAME_OVER;
        this.playerWon = playerWon;
    }

    public boolean isPlayerWon() {
        return playerWon;
    }
}
