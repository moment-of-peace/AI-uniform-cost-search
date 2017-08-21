import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;

public class Navigator {

    public static void main(String[] args) throws IOException {
        // have not implemented comparator
        
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
            String[] info = extracQue(nextLine); // info:{num1, street1, num2, street2}
            float[] startPosition = getPosition(info[0], info[1]);
            float[] goalPosition = getPosition(info[2], info[3]);
            
            PriorityQueue<Junction> queue = new PriorityQueue<Junction>(); // priority queue
            HashSet<Junction> found = new HashSet<Junction>(); // store all found junctions
            
            // put start and end junctions of the street which initial point lies on
            // into priority queue, and set their cost
            Junction init1 = getStartJunc(info[1]);
            Junction init2 = getEndJunc(info[1]);
            
            init1.cost = startPosition[0];
            init2.cost = startPosition[1];
            
            queue.add(init1);
            queue.add(init2);
            
            // set the connection relationship between goal point and the junctions of
            // the street it lies on
            Junction goal = new Junction("goal");
            getStartJunc(info[3]).neighbors.put(goal, goalPosition[0]);
            getEndJunc(info[3]).neighbors.put(goal, goalPosition[1]);
            
            // A* algorithm for path search
            found.add(init1);
            found.add(init2);
            while (!queue.isEmpty() && !queue.contains(goal)) {
                Junction temp = queue.poll();
                for (Junction j: temp.neighbors.keySet()) {
                    float newCost = temp.cost + temp.neighbors.get(j);
                    if (!found.contains(j)) {
                        j.predecessor = temp;
                        j.cost = newCost;
                        found.add(j);
                    } else if (j.cost > newCost) {
                        j.predecessor = temp;
                        j.cost = newCost;
                    }
                }
            }
            
            //write result to output file
            if (queue.isEmpty()) {
                // in this case, no solution
            } else {
                // write answer
            }
        }
        br.close();
    }

    /**
     * @param the name of a street
     * @return the corresponding end junction of this street
     */
    private static Junction getEndJunc(String string) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @param the name of a street
     * @return the corresponding start junction of this street
     */
    private static Junction getStartJunc(String string) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * calculate position of a point based on street name and number
     * @return {x, y}, x and y are the distance from the point to the start and end junction
     * of the street it lies on
     */
    private static float[] getPosition(String string, String string2) {
        // TODO Auto-generated method stub
        return null;
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