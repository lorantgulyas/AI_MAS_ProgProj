package client.heuristics.unblocker;

import client.definitions.ADistance;
import client.definitions.AHeuristic;
import client.state.*;

import java.util.*;

public class Unblocker extends AHeuristic {

    private BlockedFinder[] blockFinders;
    private ADistance measurer;
    private Level level;

    private HashMap<Goal, Integer> goalBoxMap;

    public Unblocker(State initialState, ADistance measurer) {
        super(initialState);
        this.measurer = measurer;
        this.level = initialState.getLevel();
        this.goalBoxMap = this.getGoalBoxMap(initialState);
        this.blockFinders = this.getBlockFinders(initialState);
    }

    public int h(State state) {
        int hBlocks = this.sumOfBlocks(state);
        int hGoals = this.sumOfGoals(state);

        /*
        // DEBUG
        HashSet<Block> blocks = this.getBlocks(state);
        Agent[] agents = state.getAgents();
        ArrayList<String> as = new ArrayList<>();
        for (Agent agent : agents) {
            as.add("(" + agent.getId() + "," + agent.getPosition());
        }
        Box[] boxes = state.getBoxes();
        ArrayList<String> bos = new ArrayList<>();
        for (Box box : boxes) {
            bos.add("(" + box.getId() + "," + box.getLetter() + "," + box.getPosition());
        }
        ArrayList<String> bs = new ArrayList<>();
        for (Block block : blocks) {
            bs.add(block.toString(this.measurer));
        }
        ArrayList<String> gs = new ArrayList<>();
        for (Goal goal : this.level.getGoals()) {
            gs.add("(" + this.goalBoxMap.get(goal) + "," + goal.getLetter() + "," + goal.getPosition() + ")");
        }
        gs.sort(Comparator.naturalOrder());
        System.err.println("Agents: " + as);
        System.err.println("Boxes: " + bos);
        System.err.println("Goals: " + gs);
        System.err.println("Blocks: " + bs);
        System.err.println("h_blocks = " + hBlocks + ", h_goals = " + hGoals);
        System.err.println("---------");
        */

        return hBlocks + hGoals;
    }

    @Override
    public String toString() {
        return "unblocker";
    }

    private BlockedFinder[] getBlockFinders(State initialState) {
        Goal[] goals = initialState.getLevel().getGoals();
        BlockedFinder[] blockFinders = new BlockedFinder[goals.length];
        for (int i = 0; i < goals.length; i++) {
            Goal goal = goals[i];
            int boxID = this.goalBoxMap.get(goal);
            blockFinders[i] =  new BlockedFinder(initialState, this.measurer, goal, boxID);
        }
        return blockFinders;
    }

    private HashMap<Goal, Integer> getGoalBoxMap(State initialState) {
        HashMap<Goal, Integer> map = new HashMap<>();
        Goal[] goals = initialState.getLevel().getGoals();
        Box[] boxes = initialState.getBoxes();
        boolean[] usedBoxes = new boolean[boxes.length];
        Arrays.fill(usedBoxes, false);
        for (Goal goal : goals) {
            Box box = this.findClosestBoxToGoal(boxes, goal, usedBoxes);
            usedBoxes[box.getId()] = true;
            map.put(goal, box.getId());
        }
        return map;
    }

    private HashSet<Block> getBlocks(State state) {
        HashSet<Block> blocks = new HashSet<>();
        for (BlockedFinder blockFinder : this.blockFinders) {
            HashSet<Block> goalBlocks = blockFinder.getBlocks(state);
            blocks.addAll(goalBlocks);
        }
        return blocks;
    }

    private int sumOfBlocks(State state) {
        HashSet<Block> blocks = this.getBlocks(state);
        int h = 0;
        for (Block block : blocks) {
            h += block.h(this.measurer);
        }
        return h;
    }

    private int sumOfGoals(State state) {
        int h = 0;
        Agent[] agents = state.getAgents();
        Box[] boxes = state.getBoxes();
        Goal[] goals = this.level.getGoals();
        for (Goal goal : goals) {
            int boxID = this.goalBoxMap.get(goal);
            Box box = boxes[boxID];
            if (!box.getPosition().equals(goal.getPosition())) {
                // important to multiply to make it more "profitable" to reach a goal than to be close to a box
                h += 2 * this.measurer.distance(goal.getPosition(), box.getPosition());
                Agent agent = this.findClosestAgentToBox(agents, box);
                if (agent != null) {
                    h += this.measurer.distance(box.getPosition(), agent.getPosition());
                }
            }
        }
        return h;
    }

    private Box findClosestBoxToGoal(Box[] boxes, Goal goal) {
        Box closestBox = null;
        int minDistance = Integer.MAX_VALUE;
        for (Box box : boxes) {
            if (box.getLetter() == goal.getLetter()) {
                int distance = this.measurer.distance(box.getPosition(), goal.getPosition());
                if (closestBox == null || distance < minDistance) {
                    closestBox = box;
                    minDistance = distance;
                }
            }
        }
        return closestBox;
    }

    private Box findClosestBoxToGoal(Box[] boxes, Goal goal, boolean[] usedBoxes) {
        Box closestBox = null;
        int minDistance = Integer.MAX_VALUE;
        for (Box box : boxes) {
            if (!usedBoxes[box.getId()] && box.getLetter() == goal.getLetter()) {
                int distance = this.measurer.distance(box.getPosition(), goal.getPosition());
                if (closestBox == null || distance < minDistance) {
                    closestBox = box;
                    minDistance = distance;
                }
            }
        }
        return closestBox;
    }

    private Agent findClosestAgentToBox(Agent[] agents, Box box) {
        Agent closestAgent = null;
        int minDistance = Integer.MAX_VALUE;
        for (Agent agent : agents) {
            if (agent.getColor() == box.getColor()) {
                int distance = this.measurer.distance(agent.getPosition(), box.getPosition());
                if (closestAgent == null || distance < minDistance) {
                    closestAgent = agent;
                    minDistance = distance;
                }
            }
        }
        return closestAgent;
    }

}
