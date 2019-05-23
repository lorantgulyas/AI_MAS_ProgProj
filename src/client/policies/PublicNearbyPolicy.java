package client.policies;

import client.definitions.ADistance;
import client.graph.Plan;
import client.state.Agent;
import client.state.State;
import client.utils.ClosedRooms;

import java.util.ArrayList;

/**
 * Policy that only allows for sending messages to nearby agents for public actions.
 * It does, however, broadcast a message when a goal has been reached or abandoned.
 * This policy is a mix of NearbyPolicy and PublicPolicy.
 */
public class PublicNearbyPolicy extends AbstractGoalChangedOrNearby {

    private BroadcastPolicy broadcast;
    private ArrayList<Agent[]> agentMapping;

    public PublicNearbyPolicy(State initialState, int maxDistance, ADistance measurer) {
        super(initialState, maxDistance, measurer);
        ClosedRooms rooms = new ClosedRooms(initialState);
        this.broadcast = new BroadcastPolicy(initialState);

        ArrayList<ArrayList<Agent>> mapping = rooms.findAgents(initialState.getAgents());
        this.agentMapping = new ArrayList<>();
        for (ArrayList<Agent> agents : mapping) {
            this.agentMapping.add(agents.toArray(new Agent[0]));
        }
    }

    @Override
    public Iterable<Integer> receivers(Plan node, int senderID) {
        if (this.someGoalHasChanged(node, senderID))
            return this.broadcast.receivers(node, senderID);

        State state = node.getState();
        Agent[] publicAgents = this.agentMapping.get(senderID);
        Agent sender = node.getState().getAgents()[senderID];
        return this.getNearby(state, publicAgents, sender);
    }

    @Override
    public String toString() {
        return "public-nearby(" + this.maxDistance + ")";
    }

}
