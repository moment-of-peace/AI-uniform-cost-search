import java.util.HashMap;

public class Navigator {

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        String envFile = args[0];
        String queFile = args[1];
        String outFile = args[2];
        
        HashMap<String, Junction> juncSet = new HashMap<String, Junction>(); // store all found junctions
        HashMap<String, String> roadSet = new HashMap<String, String>(); //map junction points to road
        
        String nextLine;
        
        //read each line of file "envFile"
        /* while nextLine != null
         *      String[] info = extracLine(nextLine); info: {start, end, roadname, distance, ...}
         *      
         *      Junction start;
         *      Junction end
         *      
         *      if (juncSet.contain(info[0]) {
         *          start = juncSet.get(info[0]);
         *      } else {
         *          start = new Junction(infor[0]);
         *          juncSet.put(info[0], start);
         *      }
         *      
         *      //same for end point ( contain(info[1]) ...)
         *      
         *      start.neighbors.put(end, info[3];
         *      end.neighbors.put(start, info[3];
         *      
         *      roadSet.put(info[0]+info[1], info[2]);
         *      roadSet.put(info[1]+info[0], info[2]);
         *      
         */
        
        
    }

}
