import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Navigator {

    public static void main(String[] args) throws IOException {
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
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(envFile)));
        readEnvFile(br, juncMap, roadSet, roadMap);
        br.close();
        
        // read query file and answer each query
        BufferedReader br2 = new BufferedReader(new InputStreamReader(new FileInputStream(queFile)));
        FileWriter fw = new FileWriter(outFile);
        answerQueries(br2, fw, roadMap, roadSet);
        br2.close();
        fw.close();
    }
    
    /**
     * read the environment file, store all junctions, roads, and build the graph
     * @throws NumberFormatException
     * @throws IOException
     */
    private static void readEnvFile(BufferedReader br, HashMap<String, Junction> juncMap, 
            HashMap<String, String> roadSet, HashMap<String, Object[]> roadMap) 
                    throws NumberFormatException, IOException {
        String nextLine;
        while((nextLine = br.readLine()) != null) {
            String[] info = extractEnv(nextLine);   // info: {start, end, roadname, distance, nlots}
            
            // retrieve or create junctions based on their names
            Junction start = retrieveJunc(juncMap, info[0]);
            Junction end = retrieveJunc(juncMap, info[1]);
            
            // set neighborhood relations
            float dist = Float.parseFloat(info[3]);
            start.neighbors.put(end, dist);
            end.neighbors.put(start, dist);
            
            roadSet.put(info[0]+"+"+info[1], info[2]);
            roadSet.put(info[1]+"+"+info[0], info[2]);
            
            // store the road and its details
            roadMap.put(info[2], new Object[]{start, end, dist, info[4]});
        }
    }
    
    /**
     * read each query and write answers to a file
     * @throws IOException
     */
    private static void answerQueries(BufferedReader br, FileWriter fw, HashMap<String, Object[]> roadMap, 
            HashMap<String, String> roadSet) throws IOException {
        String nextLine;
        while((nextLine = br.readLine()) != null) {
            String[] info = extractQue(nextLine); // info:{num1, street1, num2, street2}
            float[] startPosition = getPosition(info[0], info[1], roadMap);
            float[] goalPosition = getPosition(info[2], info[3], roadMap);
            
            // set the init point
            Junction init = new Junction("init");
            init.neighbors.put(getStartJunc(info[1], roadMap), startPosition[0]);
            init.neighbors.put(getEndJunc(info[1], roadMap), startPosition[1]);
            
            // set the connection relationship between goal point and the junctions of
            // the street it lies on
            Junction goal = new Junction("goal");
            getStartJunc(info[3], roadMap).neighbors.put(goal, goalPosition[0]);
            getEndJunc(info[3], roadMap).neighbors.put(goal, goalPosition[1]);
            
            // A* algorithm for path search
            boolean solution = searchPath(init, goal);
            
            //write result to output file
            if (solution) {
                // write answer
                Float length = goal.cost;
                fw.write(length.toString() + ";");
                writePath(fw, goal, roadSet, info[1], info[3]);
                
            } else {
                // in this case, no solution
                fw.write("no-path\r\n");
            }
        }
    }
    
    /**
     * the algorithm used to search a path between initial point and goal point, it is based on A* algorithm
     * @return: true, if the path is found, otherwise false 
     */
    private static boolean searchPath(Junction init, Junction goal) {
        // comparator to order junction in the Priority Queue
        Comparator<Junction> junctionComparator = new Comparator<Junction>(){
            @Override
            public int compare(Junction junction1, Junction junction2){
                if (junction1.getCost()> junction2.getCost()){return 1;}
                else if (junction1.getCost()< junction2.getCost()){return -1;} 
                return 0;
            }       
        };
        PriorityQueue<Junction> queue = new PriorityQueue<Junction>(junctionComparator); // priority queue
        HashSet<Junction> found = new HashSet<Junction>(); // store all found junctions
        
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
                    // set new predecessor and cost
                    j.predecessor = temp;
                    j.cost = newCost;
                    if (queue.remove(j)) {
                        // if it is contained in the queue, update it
                        queue.add(j);
                    }
                }
            }
        }
        // return whether the path is found
        if (queue.isEmpty()) {
            return false;
        } else {
            return true;
        }
    }
    
    /**
     * write path to a file
     * @require startRoad and endRoad are different
     * @param startRoad 
     * @param endRoad 
     * @param fw: FileWriter object
     * @param goal: the goal of the path
     * @param roadSet: a HashMap: Junctions names -> road name.
     * @throws IOException 
     */
    private static void writePath(FileWriter fw, Junction goal, HashMap<String, String> roadSet, 
            String startRoad, String endRoad) throws IOException {
        fw.write(endRoad);
        Junction temp = goal.predecessor;
        String road = roadSet.get(temp.name + "+" + temp.predecessor.name);
        fw.write("-" + temp.name);
        while (road != null) {
            fw.write("-" + road);
            temp = temp.predecessor;
            fw.write("-" + temp.name);
            road = roadSet.get(temp.name + "+" + temp.predecessor.name);
        }
        fw.write("-" + startRoad + "\r\n");
    }

    /**
     * @param the name of a street
     * @return the corresponding end junction of this street
     */
    private static Junction getEndJunc(String roadName, HashMap<String, Object[]> roadMap) {
        // Retrieve the end junction of a given road
        Junction endJunc = (Junction) roadMap.get(roadName)[1];
        return endJunc;
    }

    /**
     * @param the name of a street
     * @return the corresponding start junction of this street
     */
    private static Junction getStartJunc(String roadName, HashMap<String, Object[]> roadMap) {
        // Retrieve the start junction of a given road
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
        float unit = 2 * length/nlots;
        
        int num = Integer.parseInt(Num);
        int steps = (num - 1) / 2;
        float x = (float) ((steps + 0.5) * unit);
        float y = length - x;
        return new float[]{x, y};
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
    private static String[] extractEnv(String nextLine) {
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
    private static String[] extractQue(String nextLine) {
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
