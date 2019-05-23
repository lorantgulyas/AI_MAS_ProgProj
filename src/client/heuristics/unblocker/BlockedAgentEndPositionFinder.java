package client.heuristics.unblocker;

import client.definitions.ADistance;
import client.state.*;

import java.util.ArrayList;
import java.util.HashSet;

public class BlockedAgentEndPositionFinder {

    private ADistance measurer;
    private AgentGoal agentEndPosition;

    public BlockedAgentEndPositionFinder(ADistance measurer, AgentGoal agentEndPosition) {
        this.measurer = measurer;
        this.agentEndPosition = agentEndPosition;
    }

    public HashSet<Block> getBlocks(State state) {
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
        Path pathHelper = new Path(state, agentEndPosition, agent);
        ArrayList<Position> path = pathHelper.getPath();
        int n = path.size();
        for (int i = 1; i < n - 1; i++) {
            Position position = path.get(i);
            // there is probably no need to consider an agent many steps ahead from the current agent
            // since this agent is likely to move out of the way anyway
            boolean hasAgent = i == 1 && state.agentAt(position);
            boolean hasBox = state.boxAt(position) && state.getBoxAt(position).getColor() != agent.getColor();
            if (hasAgent || hasBox) {
                boolean blocked = pathHelper.blocked(state, agent, path.get(i - 1), path.get(i + 1), 4);
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
