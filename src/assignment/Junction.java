package assignment;
/**
 * Represent a junction defined in the environment file
 */
import java.util.HashMap;
import java.util.HashSet;

public class Junction {
    protected String name;
    protected HashMap<Junction, Road> neighbor;
    protected Junction predecessor;
    protected float cost;
    
    public Junction(String name) {
        this.name = name;
        this.neighbor = new HashMap<Junction, Road>();
        this.cost = Float.POSITIVE_INFINITY;
        this.predecessor = null;
    }
    
    public HashSet<Junction> getNeighbors() {
        HashSet<Junction> result = new HashSet<Junction>(neighbor.keySet());
        result.remove(predecessor);
        return result;
    }
}
