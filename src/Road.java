
public class Road {
    Junction start;
    Junction end;
    float length;
    int nlots;
    
    public Road(Junction start, Junction end, float length, int nlots) {
        this.start = start;
        this.end = end;
        this.length = length;
        this.nlots = nlots;
    }

}
