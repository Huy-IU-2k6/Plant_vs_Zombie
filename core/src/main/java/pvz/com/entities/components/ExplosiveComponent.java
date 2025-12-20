package pvz.com.entities.components;

public class ExplosiveComponent {

    public int damage;
    public float range;
    

    public float fuseTime;      
    


    public float timer;         
    

    public boolean hasExploded; 

    public ExplosiveComponent(int damage, float range, float fuseTime) {
        this.damage = damage;
        this.range = range;
        this.fuseTime = fuseTime;
        

        this.timer = 0f;
        this.hasExploded = false;
    }
}