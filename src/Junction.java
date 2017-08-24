import java.util.HashMap;

public class Junction {
    protected String name;
    protected HashMap<Junction, Float> neighbors; // next junction and the distance
    protected HashMap<Junction, Road> neighbor;
    protected Junction predecessor;
    protected float cost;
    
    public Junction(String name) {
        this.name = name;
        this.neighbors = new HashMap<Junction, Float>();
        this.neighbor = new HashMap<Junction, Road>();
        this.cost = Float.POSITIVE_INFINITY;
        this.predecessor = null;
    }

    public float getCost(){
        return this.cost;
    }
}