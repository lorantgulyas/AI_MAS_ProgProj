package client.heuristics.unblocker;

import client.definitions.ADistance;
import client.path.AllObjectsAStar;
import client.path.WallOnlyAStar;
import client.state.*;

import java.util.ArrayList;
import java.util.HashSet;

class BlockedAgentEndPositionFinder {

    private ADistance measurer;
    private AgentGoal agentEndPosition;
    private AllObjectsAStar objectPlanner;
    private WallOnlyAStar wallPlanner;

    BlockedAgentEndPositionFinder(
            ADistance measurer,
            AgentGoal agentEndPosition,
            AllObjectsAStar objectPlanner,
            WallOnlyAStar wallPlanner
    ) {
        this.measurer = measurer;
        this.agentEndPosition = agentEndPosition;
        this.objectPlanner = objectPlanner;
        this.wallPlanner = wallPlanner;
    }

    HashSet<Block> getBlocks(State state) {
        if (this.agentEndPositionIsFulfilled(state)) {
            return new HashSet<>();
        }
        return this.findBlocksForAgentEndPosition(state);
    }

    private boolean agentEndPositionIsFulfilled(State state) {
        Agent[] agents = state.getAgents();
        Agent agent = agents[this.agentEndPosition.getAgentID()];
        return agent.getPosition().equals(this.agentEndPosition.getPosition());
    }

    private HashSet<Block> findBlocksForAgentEndPosition(State state) {
        HashSet<Block> blocks = new HashSet<>();
        Agent[] agents = state.getAgents();
        Agent agent = agents[this.agentEndPosition.getAgentID()];
        ArrayList<Position> path = PathHelper.getPath(this.wallPlanner, state, agentEndPosition, agent);
        int n = path.size();
        for (int i = 1; i < n - 1; i++) {
            Position position = path.get(i);
            // there is probably no need to consider an agent many steps ahead from the current agent
            // since this agent is likely to move out of the way anyway
            boolean hasAgent = i == 1 && state.agentAt(position);
            boolean hasBox = state.boxAt(position) && state.getBoxAt(position).getColor() != agent.getColor();
            if (hasAgent || hasBox) {
                Position previous = path.get(i - 1);
                Position next = path.get(i + 1);
                boolean blocked = PathHelper.isBlocked(this.objectPlanner, state, agent, previous, next);
                if (blocked) {
                    if (hasAgent) {
                        Agent blockingAgent = state.getAgentAt(position);
                        Block block = new Block(blockingAgent, i);
                        blocks.add(block);
                    } else {
                        Box blockingBox = state.getBoxAt(position);
                        Agent responsibleAgent = this.findClosestAgentToBox(state, blockingBox);
                        Block block = new Block(blockingBox, responsibleAgent, i);
                        blocks.add(block);
                    }
                }
            }
        }
        return blocks;
    }

    private Agent findClosestAgentToBox(State state, Box box) {
        Agent[] agents = state.getAgents();
        Agent closestAgent = null;
        int minDistance = Integer.MAX_VALUE;
        for (Agent agent : agents) {
            if (agent.getColor() == box.getColor()) {
                int distance = this.measurer.distance(state, agent.getPosition(), box.getPosition());
                if (closestAgent == null || distance < minDistance) {
                    closestAgent = agent;
                    minDistance = distance;
                }
            }
        }
        return closestAgent;
    }

}
