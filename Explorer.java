import uk.ac.warwick.dcs.maze.logic.IRobot;
import java.util.ArrayList;
/**
 * Controls a robot to explore a maze using various navigation strategies
 * based on the number of available exits (deadend, corridor, junction, crossroad).
 * 
 * @author Nevin Ahluwalia
 * @version 1.0
 */
public class Explorer {
    /**
    * Main control method called by the maze simulator to determine
    * the robot's next move based on the number of non-wall exits.
    * 
    * @param robot The IRobot interface providing access to maze information
    */
    public void controlRobot(IRobot robot) {
        int direction = 0;
        int exits = nonwallExits(robot);
        System.out.println(exits);
        switch (exits){
            case 1:
                direction = deadend(robot);
                break;

            case 2:
                direction = corridor(robot);
                break;
            
            case 3: 
                direction = junction_and_crossroad(robot);
                break;

            case 4:
                direction = junction_and_crossroad(robot);
                break;
        }

        robot.face(direction);
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
     * Checks all four directions and randomly chooses from available non-wall directions.
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
     * Finds and returns a random passage exit from the first four directions (LEFT, RIGHT, AHEAD, BEHIND).
     * Passages are preferred over visited squares for exploration.
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
     * 
     * @param robot The IRobot interface to check surrounding squares
     * @return The chosen direction constant (IRobot.AHEAD, LEFT, RIGHT, or BEHIND)
     */
    private int junction_and_crossroad(IRobot robot){
        int final_dir; 
        if (passageExits(robot) != 0){
            final_dir = passageExits(robot);
        } else {
            final_dir = random_avoid_wall(robot);
        }
        return final_dir;
    }


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

class RobotData {

}