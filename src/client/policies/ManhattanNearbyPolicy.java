package client.policies;

import client.definitions.AMessagePolicy;
import client.distance.LazyManhattan;
import client.state.*;

import java.util.ArrayList;

/**
 * Policy that dictates to send a message to all agents that are within a configurable
 * manhattan distance of the sending agent but reverts to broadcast all of
 * its actions if it has reached a goal.
 */
public class ManhattanNearbyPolicy extends AMessagePolicy {

    private int maxDistance;
    private LazyManhattan manhattan;
    private BroadcastPolicy broadcast;

    public ManhattanNearbyPolicy(State initialState, int maxDistance) {
        super(initialState);
        this.maxDistance = maxDistance;
        this.manhattan = new LazyManhattan();
        this.broadcast = new BroadcastPolicy(initialState);
    }

    @Override
    public Iterable<Integer> receivers(State state, int sender) {
        Level level = state.getLevel();
        Agent[] agents = state.getAgents();
        Position senderPos = agents[sender].getPosition();
        Goal[] agentGooals = level.getAgentGoals(sender);

        boolean hasReachedAGoal = false;
        for (Goal agentGoal : agentGooals) {
            Box box = state.getBoxAt(agentGoal.getPosition());
            if (box != null && box.getLetter() == agentGoal.getLetter()) {
                hasReachedAGoal = true;
                break;
            }
        }

        if (hasReachedAGoal) {
            // broadcast
            return this.broadcast.receivers(state, sender);
        } else {
            // only send to nearby agents
            ArrayList<Integer> receivers = new ArrayList<>();
            for (Agent agent : agents) {
                int id = agent.getId();
                if (id != sender && this.manhattan.distance(senderPos, agent.getPosition()) < this.maxDistance) {
                    receivers.add(id);
                }
            }
            return receivers;
        }
    }

    @Override
    public String toString() {
        return "manhattan-nearby(" + this.maxDistance + ")";
    }

}
