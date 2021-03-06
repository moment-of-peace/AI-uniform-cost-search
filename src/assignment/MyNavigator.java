package assignment;
/**
 * the main class used to search the shortest path between two junctions
 */
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

public class MyNavigator {

    public static void main(String[] args) throws IOException {
        // get arguments
        String envFile = args[0];
        String queFile = args[1];
        String outFile = args[2];
        
        // store all found junctions and its name
        HashMap<String, Junction> juncMap = new HashMap<String, Junction>();
        // map junction points to road
        HashMap<String, Road> roadMap = new HashMap<String, Road>();
        
        // read environment file
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(envFile)));
        readEnv(br, juncMap, roadMap);
        br.close();
        System.out.println("Finished read environment file");
        
        // read query file and answer each query
        BufferedReader br2 = new BufferedReader(new InputStreamReader(new FileInputStream(queFile)));
        FileWriter fw = new FileWriter(outFile);
        answerQueries(br2, fw, roadMap);
        br2.close();
        fw.close();
        System.out.println("Finished answer queries");
    }
    
    /**
     * read environment file, create Junctions and Roads, build the graph
     * @throws IOException
     */
    private static void readEnv(BufferedReader br, HashMap<String, Junction> juncMap, 
            HashMap<String, Road> roadMap) throws IOException {
        String nextLine = br.readLine();
        while (nextLine != null && !nextLine.equals("")) {
            String[] info = extractEnv(nextLine); // info: {roadname, start, end, distance, nlots}
            float dist = Float.parseFloat(info[3]);
            int nlots = Integer.parseInt(info[4]);
            
            // retrieve or create junctions based on their names
            Junction start = retrieveJunc(juncMap, info[1]);
            Junction end = retrieveJunc(juncMap, info[2]);
            
            Road road = new Road(info[0], start, end, dist, nlots);
            roadMap.put(info[0], road);
            
            // set neighborhood relations
            start.neighbor.put(end, road);
            end.neighbor.put(start, road);
            
            nextLine = br.readLine();
        }
    }
    
    /**
     * read and answer each query, write the results to a file
     * @throws IOException
     */
    private static void answerQueries(BufferedReader br, FileWriter fw, 
            HashMap<String, Road> roadMap) throws IOException {
        String nextLine = br.readLine();
        while(nextLine != null && !nextLine.equals("")) {
            String[] info = extractQue(nextLine); // info:{num1, street1, num2, street2}
            // the position of initial and goal points
            float[] startPosition = getPosition(info[1], info[0], roadMap);
            float[] goalPosition = getPosition(info[3], info[2], roadMap);
            
            if (!info[1].equals(info[3])) { // whether the initial and goal are on the same road
                // create the goal junctions
                Junction goal = new Junction("goal");
                // retrieve the junctions of the goal road and set relations between them
                // and the goal point
                Junction goalRoadStart = roadMap.get(info[3]).start;
                Junction goalRoadEnd = roadMap.get(info[3]).end;
                
                Road goalToStart = new Road(null, goal, goalRoadStart, goalPosition[0], 0);
                Road goalToEnd = new Road(null, goal, goalRoadEnd, goalPosition[1], 0);
                
                goalRoadStart.neighbor.put(goal, goalToStart);
                goalRoadEnd.neighbor.put(goal, goalToEnd);
                
                // retrieve the junctions of the initial road and set the costs and predecessors
                // of these junctions
                Junction initRoadStart = roadMap.get(info[1]).start;
                Junction initRoadEnd = roadMap.get(info[1]).end;
                
                initRoadStart.cost = startPosition[0];
                initRoadStart.predecessor = null;
                initRoadEnd.cost = startPosition[1];
                initRoadEnd.predecessor = null;
                
                // A*-based algorithm for path search
                boolean solution = searchPath(initRoadStart, initRoadEnd, goal);
                
                //write result to output file
                if (solution) {
                    // write answer
                    Float length = goal.cost;
                    fw.write(length.toString() + ";");
                    writePath(fw, goal, info[1], info[3]);
                    fw.write("\r\n");
                    
                } else {
                    // in this case, no solution
                    fw.write("no-path\r\n");
                }
            } else {
                // the initial point and goal point are on the same road
                Float length = startPosition[0] - goalPosition[0];
                if (length < 0) length = -length;
                fw.write(length.toString() + ";" + info[1] + "\r\n");
            }
            nextLine = br.readLine();
        }
    }
    
    /**
     * the algorithm used to search a path between initial point and goal point, it is based on A* algorithm
     * @return: true, if the path is found, otherwise false 
     */
    private static boolean searchPath(Junction init1, Junction init2, Junction goal) {
        // comparator to order junction in the Priority Queue
        Comparator<Junction> junctionComparator = new Comparator<Junction>(){
            @Override
            public int compare(Junction junction1, Junction junction2){
                if (junction1.cost> junction2.cost){return 1;}
                else if (junction1.cost< junction2.cost){return -1;} 
                return 0;
            }       
        };
        PriorityQueue<Junction> queue = new PriorityQueue<Junction>(junctionComparator); // priority queue
        HashSet<Junction> found = new HashSet<Junction>(); // store all found junctions
        
        // add the two initial points
        queue.add(init1);
        queue.add(init2);
        found.add(init1);
        found.add(init2);
        while (!queue.isEmpty() && !queue.peek().equals(goal)) {
            // remove the first element in the priority queue
            Junction temp = queue.poll();
            // add all neighbors of the first element to the queue
            for (Junction j: temp.neighbor.keySet()) {
                float newCost = temp.cost + temp.neighbor.get(j).length;
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
     * trace back the path from the goal point and write the path to a file
     * @throws IOException
     */
    private static void writePath(FileWriter fw, Junction goal, String startRoad, String endRoad) 
            throws IOException {
        fw.write(endRoad);
        Junction temp = goal.predecessor;
        fw.write("-" + temp.name);
        
        while (temp.predecessor != null) {
            fw.write("-" + temp.neighbor.get(temp.predecessor).name);
            temp = temp.predecessor;
            fw.write("-" + temp.name);
        }
        fw.write("-" + startRoad);
    }

    /**
     * calculate position of a point based on street name and number
     * @return {x, y}, x and y are the distance from the point to the start and end junction
     * of the street it lies on
     */
    private static float[] getPosition(String roadName, String Num, 
            HashMap<String, Road> roadMap) {
        // get the road details
        Road road = roadMap.get(roadName);
        float unit = 2 * road.length/road.nlots;
        
        int num = Integer.parseInt(Num);
        int steps = (num - 1) / 2;
        float x = (float) ((steps + 0.5) * unit);
        float y = road.length - x;
        return new float[]{x, y};
    }

    /**
     * If the junction name has been mentioned in previous lines, retrieve it from the map, 
     * if not, create new junction and store it with its name in the map.
     */
    private static Junction retrieveJunc(HashMap<String, Junction> juncMap,
            String name) {
        Junction junc = juncMap.get(name);
        if (junc == null) {
            junc = new Junction(name);
            juncMap.put(name, junc);
            return junc;
        } else {
            return junc;
        }
    }

    /**
     * extract details from a line in environment file
     * @param a line of String
     * @return a String array: {start, end, name, length, nlots}
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
        String regex = "([0-9]{1,})([^0-9]{1,}[^\\f\\n\\r\\t\\v]{0,})";
        String[] temp = nextLine.split(";");
        
        String[] info1 = parseRegex(temp[0].trim(), regex);
        String[] info2 = parseRegex(temp[1].trim(), regex);
        return new String[]{info2[0], info2[1], info1[0], info1[1]};
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

