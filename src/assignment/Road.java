package assignment;
/**
 * Represent a road between two junctions
 */
public class Road {
    String name;
    Junction start;
    Junction end;
    float length;
    int nlots;
    
    public Road(String name, Junction start, Junction end, float length, int nlots) {
        this.name = name;
        this.start = start;
        this.end = end;
        this.length = length;
        this.nlots = nlots;
    }
}