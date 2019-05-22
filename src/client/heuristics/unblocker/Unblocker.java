package client.heuristics.unblocker;

import client.definitions.ADistance;
import client.definitions.AHeuristic;
import client.state.*;

import java.util.*;

public class Unblocker extends AHeuristic {

    private BlockedFinder[] blockFinders;
    private ADistance measurer;
    private int stateSize;
    private Level level;

    private HashMap<Goal, Integer> goalBoxMap;

    public Unblocker(State initialState, ADistance measurer, int stateSize) {
        super(initialState);
        this.measurer = measurer;
        this.stateSize = stateSize;
        this.level = initialState.getLevel();
        this.goalBoxMap = this.getGoalBoxMap(initialState);
        this.blockFinders = this.getBlockFinders(initialState);
    }

    public int h(State state) {
        int h = 0;
        HashSet<Block> blocks = this.getBlocks(state);
        h += this.sumOfBlocks(blocks);
        h += this.sumOfGoals(state);
        return h;
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
            blockFinders[i] =  new BlockedFinder(initialState, this.stateSize, this.measurer, goal, boxID);
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

    private int sumOfBlocks(HashSet<Block> blocks) {
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
        boolean[] used = new boolean[agents.length];
        Arrays.fill(used, false);
        for (Goal goal : goals) {
            int boxID = this.goalBoxMap.get(goal);
            Box box = boxes[boxID];
            if (!box.getPosition().equals(goal.getPosition())) {
                h += this.measurer.distance(goal.getPosition(), box.getPosition());
                Agent agent = this.findClosestAgentToBox(agents, box, used);
                if (agent != null) {
                    h += this.measurer.distance(box.getPosition(), agent.getPosition());
                    used[agent.getId()] = true;
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

    private Agent findClosestAgentToBox(Agent[] agents, Box box, boolean[] used) {
        Agent closestAgent = null;
        int minDistance = Integer.MAX_VALUE;
        for (Agent agent : agents) {
            if (!used[agent.getId()] && agent.getColor() == box.getColor()) {
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
