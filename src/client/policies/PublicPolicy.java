package client.policies;

import client.graph.Plan;
import client.state.*;
import client.utils.ClosedRooms;

import java.util.ArrayList;

/**
 * Policy that is described in the multi-agent A* paper.
 * It only sends messages to other agents whose public variables
 * are somehow affected by an action.
 */
public class PublicPolicy extends AbstractGoalChanged {

    private BroadcastPolicy broadcast;
    private ArrayList<ArrayList<Integer>> agentMapping;

    public PublicPolicy(State initialState) {
        super(initialState);
        ClosedRooms rooms = new ClosedRooms(initialState);
        this.broadcast = new BroadcastPolicy(initialState);

        ArrayList<ArrayList<Agent>> mapping = rooms.findAgents(initialState.getAgents());
        this.agentMapping = new ArrayList<>();
        for (ArrayList<Agent> agents : mapping) {
            ArrayList<Integer> mapTo = new ArrayList<>();
            for (Agent agent : agents) {
                mapTo.add(agent.getId());
            }
            this.agentMapping.add(mapTo);
        }
    }

    @Override
    public Iterable<Integer> receivers(Plan node, int sender) {
        if (this.someGoalHasChanged(node, sender))
            return this.broadcast.receivers(node, sender);

        return this.agentMapping.get(sender);
    }

    @Override
    public String toString() {
        return "public";
    }
}
