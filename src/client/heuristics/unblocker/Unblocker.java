package client.heuristics.unblocker;

import client.definitions.ADistance;
import client.definitions.AHeuristic;
import client.state.*;

import java.util.ArrayList;
import java.util.Arrays;

public class Unblocker extends AHeuristic {

    private BlockedFinder[] agents;
    private ADistance measurer;
    private int stateSize;
    private Level level;

    public Unblocker(State initialState, ADistance measurer, int stateSize) {
        super(initialState);
        this.measurer = measurer;
        this.stateSize = stateSize;
        this.level = initialState.getLevel();
        Agent[] agents = initialState.getAgents();
        this.agents = new BlockedFinder[agents.length];
        for (Agent agent : agents) {
            this.agents[agent.getId()] =  new BlockedFinder(agent.getId(), initialState, measurer);
        }
    }

    public int h(State state) {
        int h = 0;
        ArrayList<Block> blocks = this.getBlocks(state);
        h += this.sumOfBlocks(blocks);
        h += this.sumOfGoals(state);
        return h;
    }

    @Override
    public String toString() {
        return "unblocker";
    }

    private ArrayList<Block> getBlocks(State state) {
        ArrayList<Block> blocks = new ArrayList<>();
        for (BlockedFinder agent : this.agents) {
            ArrayList<Block> agentBlocks = agent.getBlocks(state);
            blocks.addAll(agentBlocks);
        }
        return blocks;
    }

    private int sumOfBlocks(ArrayList<Block> blocks) {
        int h = 0;
        for (Block block : blocks) {
            Agent agent = block.getResponsible();
            Box boxToMove = block.getBox();
            if (boxToMove == null) {
                h += block.getValue();
            } else {
                h += this.measurer.distance(agent.getPosition(), boxToMove.getPosition()) + block.getValue();
            }
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
        int nUnfulfilledGoals = 0;
        for (Goal goal : goals) {
            Box box = this.findClosestBoxToGoal(boxes, goal);
            if (box != null && !box.getPosition().equals(goal.getPosition())) {
                h += this.measurer.distance(goal.getPosition(), box.getPosition());
                Agent agent = this.findClosestAgentToBox(agents, box, used);
                if (agent != null) {
                    h += this.measurer.distance(box.getPosition(), agent.getPosition());
                    used[agent.getId()] = true;
                }
            }

            if (box == null || !box.getPosition().equals(goal.getPosition())) {
                nUnfulfilledGoals++;
            }
        }
        return h + nUnfulfilledGoals * this.stateSize;
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
