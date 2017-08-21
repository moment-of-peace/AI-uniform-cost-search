import java.util.HashMap;

public class Junction {
    protected String name;
    protected HashMap<Junction, Float> neighbors; // next junction and the distance
    protected Junction predecessor;
    protected float cost;
    
    public Junction(String name) {
        this.name = name;
        this.neighbors = new HashMap<Junction, Float>();
        this.cost = Float.POSITIVE_INFINITY;
    }
    
}