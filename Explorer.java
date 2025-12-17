import uk.ac.warwick.dcs.maze.logic.IRobot;
import java.util.ArrayList;


//So if as long as we have the relative direction
// then we just need to find the junction that matches said relative direction
// we can do this by converting the arrivedFrom heading to a relative direction
// Then we just backtrack until we find said direction

// Lists of things we need to do to implement this i.e design steps 
// 1. Remove all the code that needs a location
// 2. In the reverse direction code we need to add some extra code
// this says look if we are at a junction check does the arrivedFrom match
// the relative direction we were looking for if it does then yes 
// head down that junction

//List of issues
//What 

/**
 * Controls a robot to explore a maze using various navigation strategies
 * based on the number of available exits (deadend, corridor, junction, crossroad).
 * 
 * @author Nevin Ahluwalia
 * @version 1.0
 */
public class Explorer {
    private int pollRun = 0; // Incremented after each pass
    private RobotData robotData;
    private int explorerMode = 1; // 1 = explore, 0 = backtrack
    /**
     * Main control method called by the maze simulator to choose the 
     * robots next move. Delegates to either exploreControl or backtrackControl
     * depending on the current exploration mode/
     * 
     * @param robot the {@link IRobot} interface providing access to maze information
     */
    public void controlRobot(IRobot robot) {
        if ((robot.getRuns() == 0) && (pollRun == 0)){
            robotData = new RobotData();
            explorerMode = 1; 
        }
        pollRun++;
        if (explorerMode == 1){
            exploreControl(robot);
        } else {
            backtrackControl(robot);
        }
    }

    /**
     * Control the robot's explorataion behaviour when it is in explorer mode 
     * This will handkle navigation at any number of exits and records 
     * new junctions encountered and switches to backtrack mode when a deadend
     * is reached
     * 
     * @param robot the {@link IRobot} interface providing access to maze information
     */
    public void exploreControl(IRobot robot){

        int direction = 0;
        int exits = nonwallExits(robot);
        switch (exits){
            case 1:
                direction = deadend(robot);
                if (pollRun > 1) {  // Not the first move 
                    explorerMode = 0;
                }
                break;

            case 2:
                direction = corridor(robot);
                break;
            
            case 3: case 4: 
                direction = junction_and_crossroad(robot);
                break;
        }

        int unencountered_or_not = beenbeforeExits(robot);
        if ((unencountered_or_not < 1) && (exits == 3 || exits == 4)){
            int x = robot.getLocation().x;
            int y = robot.getLocation().y;
            int arrivedFrom = robot.getHeading();
            robotData.recordJunction(x,y, arrivedFrom);
        }
        robot.face(direction);

    }

    /**
     * Controls the robot's backtracking behaviour when it backtrack mode 
     * At junctions or crossroads it switches to explore mode if there is passage exit
     * or if there i a junction that hasn't been encountered , otherwise it will backtrack
     * in the oppoisite dir from which it first entered the junction 
     * At corridors and deadends: continues backtracking by moving forwards 
     * 
     * @param robot the {@link IRobot} interface providing access to maze information
     */
    public void backtrackControl(IRobot robot){ 
        int final_heading;
        int nonwallExits = nonwallExits(robot);
        int x = robot.getLocation().x;
        int y = robot.getLocation().y;
        int arrivedFrom = robot.getHeading();
        if (nonwallExits > 2){
            if (passageExits(robot) > 0){ 
                explorerMode = 1;
                int direction = passageExits(robot);
                robotData.recordJunction(x ,y, arrivedFrom);
                robot.face(direction);
            } else {
                int res = robotData.searchJunction(x,y);
                final_heading = (res == 1000 || res == 1001) ? res + 2 : res - 2;
                robot.setHeading(final_heading);
            }
        } else if (nonwallExits == 2){
            final_heading = corridor(robot);
            robot.face(final_heading);
        } else {
            final_heading = deadend(robot);
            robot.face(final_heading);
        }
    }

    /**
     * Resets the junction counter in the associated {@link RobotData} instance.
     * This is called by the simulator when a new run of the maze begins so
     * that old junction information does not affect a fresh exploration.
     */
    public void reset() {
        robotData.resetJunctionCounter();
        explorerMode = 1;
    }
    
    /**
     * Counts the number of directions that are not blocked by walls.
     * 
     * @param robot The IRobot interface to check surrounding squares
     * @return The number of non-wall exits (1-4)
     */
    private int nonwallExits (IRobot robot) { 
        int directions[] = {IRobot.AHEAD, IRobot.BEHIND, IRobot.LEFT, IRobot.RIGHT};
        int non_wall_square = 4;
        
        for (int i = 0; i < 4; i++){
            int wallornot = robot.look(directions[i]);
            if (wallornot == IRobot.WALL){
                non_wall_square -= 1;
            }
        }
        return non_wall_square;
    }

    /**
     * Selects a random direction that is not blocked by a wall.
     * Checks all four directions and randomly chooses from a direction that isn't a wall
     * 
     * @param robot The IRobot interface to check surrounding squares
     * @return A random direction constant (IRobot.AHEAD, LEFT, RIGHT, or BEHIND) that is not a wall
     */
    private int random_avoid_wall(IRobot robot){
        int directions[] = {IRobot.AHEAD, IRobot.LEFT, IRobot.RIGHT, IRobot.BEHIND};
        ArrayList<Integer> available_directions = new ArrayList<>();
        for (int i = 0; i < 4; i++){
            if (robot.look(directions[i]) != IRobot.WALL){
                available_directions.add(directions[i]);
            }
        }
        int randIndex = (int)(Math.random() * available_directions.size());
        int non_wall_dir = available_directions.get(randIndex);

        return non_wall_dir;
    }

    /**
     * Finds and returns a random passage exit from the four directions.
     * Passages are preferred over BEENBEFORE squares for exploration.
     * If there are more than one random passages available choose a 
     * direction randomly out of them
     * 
     * @param robot The IRobot interface to check surrounding squares
     * @return A random passage direction if available, or 0 if no passages are found
     */
    private int passageExits(IRobot robot){
        int final_dir = 0; 
        int passage_exit;
        ArrayList<Integer> available_directions = new ArrayList<>();
        int directions[] = {IRobot.LEFT, IRobot.RIGHT, IRobot.AHEAD, IRobot.BEHIND};
        for (int i = 0; i < 4; i++){
            if (robot.look(directions[i]) == IRobot.PASSAGE){
                available_directions.add(directions[i]);
            }
        } 
        if (available_directions.size() != 0){
            int randIndex = (int)(Math.random() * available_directions.size());
            passage_exit = available_directions.get(randIndex);
        } else {
            passage_exit = 0;
        }
        return passage_exit;
    }

    /**
     * Handles navigation at a deadend (1 exit). Since there's only one way out,
     * selects the single non-wall direction available.
     * 
     * @param robot The IRobot interface to check surrounding squares
     * @return The direction constant for the only non-wall exit
     */
    private int deadend(IRobot robot){
        int final_dir = random_avoid_wall(robot);
        return final_dir;
    }

    /**
     * Handles navigation in a corridor (2 exits). Prevents the robot from going backwards
     * by repeatedly selecting a random non-wall direction until it's not BEHIND.
     * 
     * @param robot The IRobot interface to check surrounding squares
     * @return A random non-wall direction that is not BEHIND
     */
    private int corridor(IRobot robot){
        int final_dir = 0;
        do {
            final_dir = random_avoid_wall(robot);
        } while (final_dir == IRobot.BEHIND);
        return final_dir; 
    }

    /**
     * Handles navigation at a junction (3 exits) and crossroads (4 exits). Prefers moving into passage squares
     * if available, otherwise chooses a random non-wall direction.
     * If there is a passage exit then choose that direction, else randomly explore
     * 
     * @param robot The IRobot interface to check surrounding squares
     * @return The chosen direction constant (IRobot.AHEAD, LEFT, RIGHT, or BEHIND)
     */
    private int junction_and_crossroad(IRobot robot){
        int final_dir = (passageExits(robot) != 0) ? passageExits(robot) : random_avoid_wall(robot);
        return final_dir;
    }


    /**
     * Counts the number of neighbouring squares which have been visited before.
     *
     * @param robot the {@link IRobot} used to inspect the surrounding squares
     * @return the number of exits that lead to {@code BEENBEFORE} squares
     */
    private int beenbeforeExits(IRobot robot){
        int num_beenbefore = 0;
        int directions[] = {IRobot.LEFT, IRobot.RIGHT, IRobot.AHEAD, IRobot.BEHIND};
        for (int i = 0; i < 3; i++){
            if (robot.look(directions[i]) == IRobot.BEENBEFORE){
                num_beenbefore++;
            }
        }
        return num_beenbefore;
    }

}

/**
 * This is a data holder class that records information about a single junction:
 * its coordinates within the maze and heading from which the robot was in when it
 * first arrived at the junction
 */
class JunctionRecorder { 
    private int x;
    private int y;
    private int arrivedFrom;

    /**
     * Constructs a new {@code JunctionRecorder} with the supplied details.
     *
     * @param x the x-coordinate of the junction
     * @param y the y-coordinate of the junction
     * @param arrivedFrom the heading from which the robot first arrived
     *                    at this junction
     */
    public JunctionRecorder(int x, int y , int arrivedFrom) {
        this.x = x; 
        this.y = y;
        this.arrivedFrom = arrivedFrom;
    }
    /**
     * Returns the x-coordinate of this junction.
     *
     * @return the x-coordinate
     */
    public int getX() { return x; }

    /**
     * Returns the y-coordinate of this junction.
     *
     * @return the y-coordinate
     */
    public int getY() { return y; }

    /**
     * Returns the heading from which the robot first arrived at this junction.
     *
     * @return the arrival heading
     */
    public int getArrivedFrom() { return arrivedFrom; }

}

/**
 * Stores information about all junctions encountered during a run of the maze.
 * The class has an array of {@link JunctionRecorder} objects and a
 * counter indicating how many junctions have been recorded.
 */
class RobotData {
    private static int maxJunctions = 10000;
    private static int junctionCounter; 
    private JunctionRecorder[] junctions;
    
    /**
     * Resets the junction counter back to zero. This is called when a new run
     * of the maze begins so that recording starts from the first array element.
     */
    public void resetJunctionCounter() {
        junctionCounter = 0;
    }

    /**
     * Constructs a new {@code RobotData} instance with an empty array of
     * {@link JunctionRecorder} objects and the junction counter reset to zero.
     */
    public RobotData() {
        junctions = new JunctionRecorder[maxJunctions];
        junctionCounter = 0;
    }


    /**
     * Records a new junction in the array and prints its details.
     *
     * @param x the x-coordinate of the junction
     * @param y the y-coordinate of the junction
     * @param arrivedFrom the heading from which the robot first arrived
     *                    at the junction
     */
    public void recordJunction(int x, int y, int arrivedFrom ){
        junctions[junctionCounter] = new JunctionRecorder(x, y, arrivedFrom);
        printJunction(junctionCounter);
        junctionCounter++;
    }

    /**
     * Prints information about the junction stored at the given index.
     * The information includes which number junction it is, its coordinates and
     * the heading from which the robot first arrived there.
     *
     * @param index the index of the junction to be printed
     */
    public void printJunction(int index){
        JunctionRecorder j = junctions[index];
        int x = j.getX();
        int y = j.getY();
        int arrivedFrom = j.getArrivedFrom();
        String direction = (arrivedFrom == 1000) ? "NORTH" :
                           (arrivedFrom == 1001) ? "EAST" :
                           (arrivedFrom == 1002) ? "SOUTH" :
                           (arrivedFrom == 1003) ? "WEST" : "UNKNOWN";
    }

    /**
     * Searches for a junction with the given coordinates in the recorded
     * junction data. Returns the absolute heading from which the robot first
     * arrived at that junction, or -1 if the junction has not been encountered before.
     *
     * @param x the x-coordinate of the junction to search for
     * @param y the y-coordinate of the junction to search for
     * @return the absolute heading (1000=NORTH, 1001=EAST, 1002=SOUTH, 1003=WEST)
     *         when the robot first arrived at this junction, or -1 if not found
     */
    public int searchJunction(int x , int y){
        for (int i = 0; i < junctionCounter; i++){
            JunctionRecorder j = junctions[i];
            int x_junction = j.getX();
            int y_junction = j.getY();
            int arrivedFrom = j.getArrivedFrom();
            if (x == x_junction && y == y_junction) {
                return arrivedFrom;

            }
        }
        return -1;
    }
}