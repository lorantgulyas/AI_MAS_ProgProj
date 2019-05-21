package client.heuristics.unblocker;

import client.definitions.ADistance;
import client.state.*;

import java.util.ArrayList;

public class BlockedFinder {

    private Goal goal;
    private ADistance measurer;
    private Level level;
    private int boxID;
    private ArrayList<Integer> goalAgentIDs;
    private int stateSize;

    public BlockedFinder(State initialState, int stateSize, ADistance measurer, Goal goal, int boxID) {
        this.level = initialState.getLevel();
        this.measurer = measurer;
        this.goal = goal;
        this.boxID = boxID;
        this.goalAgentIDs = this.getGoalAgentIDs(initialState);
        this.stateSize = stateSize;
    }

    public ArrayList<Block> getBlocks(State state) {
        // 1. check if goal is fulfilled and return empty array if it is fulfilled
        // 2. check if it is possible to fulfill this goal
        //   a. if not: find where the block is and add it
        //   b. value of block should be the amount of moves to make it unblocked
        if (this.goalIsFulfilled(state)) {
            return new ArrayList<>();
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

    //private boolean goalCanBeFulfilled(State state) {
    //    boolean[][] walls = this.level.getWalls();
    //    Agent[] agents = state.getAgents();
    //    for (Agent agent : agents) {
    //        if (agent.getColor() == this.goal.getColor()) {
    //            ArrayList<Position> path = Path.bfs(walls, goal.getPosition(), agent.getPosition());
    //            if (path.size() != 0) {
    //                return true;
    //            }
    //        }
    //    }
    //}

    private ArrayList<Block> findBlocksForGoal(State state) {
        ArrayList<Block> blocks = new ArrayList<>();
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
                boolean hasAgent = state.agentAt(position);
                boolean hasBox = state.boxAt(position);
                if (hasAgent || hasBox) {
                    boolean blocked = pathHelper.blocked(state, agent, path.get(i - 1), path.get(i + 1), 7);
                    if (blocked) {
                        if (hasAgent) {
                            Agent blockingAgent = state.getAgentAt(position);
                            Block block = new Block(blockingAgent, i);
                            blocks.add(block);
                        } else {
                            Box blockingBox = state.getBoxAt(position);
                            if (blockingBox.getColor() != agent.getColor()) {
                                Agent responsibleAgent = this.findClosestAgentToBox(agents, blockingBox);
                                Block block = new Block(blockingBox, responsibleAgent, i);
                                blocks.add(block);
                            }
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
