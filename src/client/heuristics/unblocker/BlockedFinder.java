package client.heuristics.unblocker;

import client.definitions.ADistance;
import client.state.*;

import java.util.ArrayList;
import java.util.HashSet;

public class BlockedFinder {

    private Goal goal;
    private ADistance measurer;
    private int boxID;
    private ArrayList<Integer> goalAgentIDs;
    private int stateSize;

    public BlockedFinder(State initialState, int stateSize, ADistance measurer, Goal goal, int boxID) {
        this.measurer = measurer;
        this.goal = goal;
        this.boxID = boxID;
        this.goalAgentIDs = this.getGoalAgentIDs(initialState);
        this.stateSize = stateSize;
    }

    public HashSet<Block> getBlocks(State state) {
        if (this.goalIsFulfilled(state)) {
            return new HashSet<>();
        }
        return this.findBlocksForGoal(state);
    }

    private ArrayList<Integer> getGoalAgentIDs(State initialState) {
        ArrayList<Integer> agentIDs = new ArrayList<>();
        Agent[] agents = initialState.getAgents();
        for (Agent agent : agents) {
            if (agent.getColor() == this.goal.getColor()) {
                agentIDs.add(agent.getId());
            }
        }
        return agentIDs;
    }

    private boolean goalIsFulfilled(State state) {
        Box box = state.getBoxAt(goal.getPosition());
        return box != null && box.getLetter() == this.goal.getLetter();
    }

    private HashSet<Block> findBlocksForGoal(State state) {
        HashSet<Block> blocks = new HashSet<>();
        Agent[] agents = state.getAgents();
        Box[] boxes = state.getBoxes();
        Box box = boxes[this.boxID];
        for (int agentID : this.goalAgentIDs) {
            Agent agent = agents[agentID];
            Path pathHelper = new Path(state, this.stateSize, this.goal, box, agent);
            ArrayList<Position> path = pathHelper.getPath();
            int n = path.size();
            for (int i = 1; i < n - 1; i++) {
                Position position = path.get(i);
                boolean hasAgent = state.agentAt(position) && state.getAgentAt(position).getId() != agent.getId();
                boolean hasBox = state.boxAt(position) && state.getBoxAt(position).getColor() != agent.getColor();
                if (hasAgent || hasBox) {
                    boolean blocked = pathHelper.blocked(state, agent, path.get(i - 1), path.get(i + 1), 7);
                    if (blocked) {
                        if (hasAgent) {
                            Agent blockingAgent = state.getAgentAt(position);
                            Block block = new Block(blockingAgent, i);
                            blocks.add(block);
                        } else {
                            Box blockingBox = state.getBoxAt(position);
                            Agent responsibleAgent = this.findClosestAgentToBox(agents, blockingBox);
                            Block block = new Block(blockingBox, responsibleAgent, i);
                            blocks.add(block);
                        }
                    }
                }
            }
        }
        return blocks;
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
