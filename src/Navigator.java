
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;

public class Navigator {

    public static void main(String[] args) throws IOException {
        // have not implemented comparator
        
        // get arguments
        String envFile = args[0];
        String queFile = args[1];
        String outFile = args[2];
        
        // store all found junctions and its name
        HashMap<String, Junction> juncMap = new HashMap<String, Junction>(); 
        // map junction points to road
        HashMap<String, String> roadSet = new HashMap<String, String>(); 
        // a map used to store road details
        HashMap<String, Object[]> roadMap = new HashMap<String, Object[]>();
        
        // read environment file
        String nextLine;
        BufferedReader br = null;
        br = new BufferedReader(new InputStreamReader(new FileInputStream(envFile)));
        
        while((nextLine = br.readLine()) != null) {
            String[] info = extracEnv(nextLine);   // info: {start, end, roadname, distance, nlots}
            
            // retrieve or create junctions based on their names
            Junction start = retrieveJunc(juncMap, info[0]);
            Junction end = retrieveJunc(juncMap, info[1]);
            
            // set neighborhood relations
            float dist = Float.parseFloat(info[3]);
            start.neighbors.put(end, dist);
            end.neighbors.put(start, dist);
            
            roadSet.put(info[0]+info[1], info[2]);
            roadSet.put(info[1]+info[0], info[2]);
            
            // store the road and its details
            roadMap.put(info[2], new Object[]{start, end, dist, info[4]});
        }
        br.close();
        
        // read query file
        br = new BufferedReader(new InputStreamReader(new FileInputStream(queFile)));
        while((nextLine = br.readLine()) != null) {
            String[] info = extracQue(nextLine); // info:{num1, street1, num2, street2}
            float[] startPosition = getPosition(info[0], info[1], roadMap);
            float[] goalPosition = getPosition(info[2], info[3], roadMap);
            
            PriorityQueue<Junction> queue = new PriorityQueue<Junction>(); // priority queue
            HashSet<Junction> found = new HashSet<Junction>(); // store all found junctions
            
            // put start and end junctions of the street which initial point lies on
            // into priority queue, and set their cost
            Junction init1 = getStartJunc(info[1], roadMap);
            Junction init2 = getEndJunc(info[1], roadMap);
            
            init1.cost = startPosition[0];
            init2.cost = startPosition[1];
            
            queue.add(init1);
            queue.add(init2);
            
            // set the connection relationship between goal point and the junctions of
            // the street it lies on
            Junction goal = new Junction("goal");
            getStartJunc(info[3], roadMap).neighbors.put(goal, goalPosition[0]);
            getEndJunc(info[3], roadMap).neighbors.put(goal, goalPosition[1]);
            
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
                        queue.add(j);
                    } else if (j.cost > newCost) {
                        //in case of order change in pq
                        queue.remove(j);
                        j.predecessor = temp;
                        j.cost = newCost;
                        queue.add(j);
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
    private static Junction getEndJunc(String roadName, HashMap<String, Object[]> roadMap) {
        // Retrive the end junction of a given road
    	Junction endJunc = (Junction) roadMap.get(roadName)[1];
        return endJunc;
    }

    /**
     * @param the name of a street
     * @return the corresponding start junction of this street
     */
    private static Junction getStartJunc(String roadName, HashMap<String, Object[]> roadMap) {
        // Retrive the start junction of a given road
    	Junction startJunc = (Junction) roadMap.get(roadName)[0];
        return startJunc;
    }

    /**
     * calculate position of a point based on street name and number
     * @return {x, y}, x and y are the distance from the point to the start and end junction
     * of the street it lies on
     */
    private static float[] getPosition(String roadName, String Num, 
            HashMap<String, Object[]> roadMap) {
        // get the road details
    	float length = (float) roadMap.get(roadName)[2];
        float nlots = Float.parseFloat((String) roadMap.get(roadName)[3]);
        float unit = length/nlots;
        
        int num = Integer.parseInt(Num);
        int steps = (num + 1) / 2;
        float x = (float) ((steps + 0.5) * unit);
        float y = length - x;
        return new float[]{x, y};
    	/*float x;
    	float y;
    	int num = Integer.parseInt(Num);
    	if(num%2==0){
    		x = (num/2-1)*unit*2+unit;
    		y = length-x;
    	}else{
    		x = num/2*2*unit+unit;//get quotient
    		y = length-x;
    	}
    	float result[]= new float[]{x,y};
    	return result;*/
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

    /**
     * extract details from a line in environment file
     * @param a line of String
     * @return a String array£º{start, end, name, length, nlots}
     */
    private static String[] extracEnv(String nextLine) {
        String[] temp = nextLine.split(";");
        String[] info = new String[5];
        for (int i = 0; i < 5; i++) {
            info[i] = temp[i].trim();
        }
        return info;
    }
    
    /**
     * extract details from a line in query file
     * @param a line of String
     * @return a String array: {number 1, name 1, number 2, name 2}
     */
    private static String[] extracQue(String nextLine) {
        String regex = "([0-9]{1,})([a-zA-z]{1,}[a-zA-z0-9]{0,})";
        String[] temp = nextLine.split(";");
        
        String[] info1 = parseRegex(temp[0].trim(), regex);
        String[] info2 = parseRegex(temp[1].trim(), regex);
        return new String[]{info1[0], info1[1], info2[0], info2[1]};
    }

    /**
     * parse regular expression to extract information from query file
     * @param raw string
     * @param regular expression
     * @return
     */
    private static String[] parseRegex(String string, String regex) {
        Matcher matcher = Pattern.compile(regex).matcher(string);
        String firstPart = "";
        String secondPart = "";
        
        if (matcher.find()) {
            firstPart = matcher.group(1);
            secondPart = matcher.group(2);
        }
        return new String[]{firstPart, secondPart};
    }
}
