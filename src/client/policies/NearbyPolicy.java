package client.policies;

import client.definitions.ADistance;
import client.distance.LazyManhattan;
import client.graph.Plan;
import client.state.*;

import java.util.ArrayList;

/**
 * Policy that dictates to send a message to all agents that are within a configurable
 * manhattan distance of the sending agent but reverts to broadcasting when it is detected
 * that a goal has been reached or that a goal that used to be reached is no longer reached.
 */
public class NearbyPolicy extends AbstractGoalChangedOrNearby {

    private BroadcastPolicy broadcast;

    public NearbyPolicy(State initialState, int maxDistance, ADistance measurer) {
        super(initialState, maxDistance, measurer);
        this.broadcast = new BroadcastPolicy(initialState);
    }

    @Override
    public Iterable<Integer> receivers(Plan node, int senderID) {
        if (this.someGoalHasChanged(node, senderID))
            return this.broadcast.receivers(node, senderID);

        State state = node.getState();
        Agent[] agents = node.getState().getAgents();
        Agent sender = agents[senderID];
        return this.getNearby(state, agents, sender);
    }

    @Override
    public String toString() {
        return "nearby(" + this.maxDistance + ")";
    }

}
