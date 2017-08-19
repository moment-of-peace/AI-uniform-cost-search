import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

public class Navigator {

    public static void main(String[] args) throws IOException {
        // TODO Auto-generated method stub
        String envFile = args[0];
        String queFile = args[1];
        String outFile = args[2];
        
        // store all found junctions and its name
        HashMap<String, Junction> juncMap = new HashMap<String, Junction>(); 
        //map junction points to road
        HashMap<String, String> roadSet = new HashMap<String, String>(); 
        
        // read environment file
        String nextLine;
        BufferedReader br = null;
        br = new BufferedReader(new InputStreamReader(new FileInputStream(envFile)));
        
        while((nextLine = br.readLine()) != null) {
            String[] info = extracEnv(nextLine);   // info: {start, end, roadname, distance, ...}
            Junction start;
            Junction end;
            
            // retrieve or create junctions based on their names
            start = retrieveJunc(juncMap, info[0]);
            end = retrieveJunc(juncMap, info[1]);
            
            // set neighborhood relations
            float dist = Float.parseFloat(info[3]);
            start.neighbors.put(end, dist);
            end.neighbors.put(start, dist);
            
            roadSet.put(info[0]+info[1], info[2]);
            roadSet.put(info[1]+info[0], info[2]);
        }
        br.close();
        
        // read query file
        br = new BufferedReader(new InputStreamReader(new FileInputStream(queFile)));
        while((nextLine = br.readLine()) != null) {
            String[] info = extracQue(nextLine);
            
        }
    }

    /**
     * If the junction name has been mentioned in previous lines, retrieve it from the map, 
     * if not, create new junction and store it with its name in the map.
     */
    private static Junction retrieveJunc(HashMap<String, Junction> juncMap,
            String name) {
        if (juncMap.containsKey(name)) {
            return juncMap.get(name);
        } else {
            Junction start = new Junction(name);
            juncMap.put(name, start);
            return start;
        }
    }

    private static String[] extracEnv(String nextLine) {
        // TODO Auto-generated method stub
        return null;
    }
    
    private static String[] extracQue(String nextLine) {
        // TODO Auto-generated method stub
        return null;
    }
}