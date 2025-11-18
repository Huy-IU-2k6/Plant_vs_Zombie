package pvz.com;

public class Zombie extends Actor {
    protected int health;
    protected int speed;
    protected libgdxsound ComingZombieSound = new libgdxsound("audio/sound/ComingZombie.mp3");
    protected  static boolean GameOver = false;
    protected  static int CountZombie  = 0;
    public Zombie(){
        if(CountZombie==0){
            ComingZombieSound.play();
        }
        CountZombie+1;
    }
    protected void setSpeed(){
        if(istouchingPlant()){
            speed = 0;
        }
        else{
            Zombie=speed;
        }
    }
    protected void setHealth(int health){
        Zombiehealth -= health;
        removeTouching(bullet.class);
        dyingAnimation(filename,timeloop);  
        backyard world = (backyard)getWorld();
        counterscore score = world.getscore();
        score.addscore(point);
        world.removeObject(this);
    }
    protected void dyingAnimation(String filename, int timeloop){
        deadActor dead = new deadActor(filename,timeloop);
        World world = getWorld();
        world.addObject(dead,getX(),getY());
    }
    protected void checkGameOver(){
        backyard world = (backyard)getWorld();
        int columns = world.returngridcolumnsPosition(getX());
        if(getX()<260){
            setHealth(-1);
            Counter score=world.getscoreCounter();
            world.stopBackgroundMusic();
            GameOver=true;
            libgdx.setworld(new GameOverScreen(score.getValue()));  
        }
    }

    
}
