package client.policies;

import client.definitions.AMessagePolicy;
import client.distance.LazyManhattan;
import client.graph.Plan;
import client.state.*;

import java.util.ArrayList;

/**
 * Policy that dictates to send a message to all agents that are within a configurable
 * manhattan distance of the sending agent but reverts to broadcasting when it is detected
 * that a goal has been reached or that a goal that used to be reached is no longer reached.
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
    public Iterable<Integer> receivers(Plan node, int sender) {
        if (this.someGoalHasChanged(node, sender)) {
            // broadcast
            return this.broadcast.receivers(node, sender);
        } else {
            // only send to nearby agents
            State state = node.getState();
            Agent[] agents = state.getAgents();
            Position senderPos = agents[sender].getPosition();
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

    private boolean someGoalHasChanged(Plan node, int sender) {
        Plan parent = node.getParent();
        if (parent == null) {
            return false;
        }
        State parentState = parent.getState();
        State state = node.getState();
        Level level = state.getLevel();
        Goal[] agentGooals = level.getAgentGoals(sender);
        for (Goal agentGoal : agentGooals) {
            Position goalPosition = agentGoal.getPosition();
            char goalLetter = agentGoal.getLetter();
            Box box = state.getBoxAt(goalPosition);
            Box parentBox = parentState.getBoxAt(goalPosition);
            boolean reachedGoal = box != null && box.getLetter() == goalLetter;
            boolean parentReachedGoal = parentBox != null && parentBox.getLetter() == goalLetter;
            if ((reachedGoal && !parentReachedGoal) || (!reachedGoal && parentReachedGoal)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "manhattan-nearby(" + this.maxDistance + ")";
    }

}
