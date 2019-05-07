package client.policies;

import client.distance.LazyManhattan;
import client.state.Agent;
import client.state.Position;
import client.state.State;

import java.util.ArrayList;

/**
 * This policy is not supposed to be instantiated. Instead it serves
 * as a super class for other policies to extend.
 */
abstract class AbstractGoalChangedOrNearby extends AbstractGoalChanged {

    private LazyManhattan manhattan;

    int maxDistance;

    AbstractGoalChangedOrNearby(State initialState, int maxDistance) {
        super(initialState);
        this.maxDistance = maxDistance;
        this.manhattan = new LazyManhattan();
    }

    Iterable<Integer> getNearby(Agent[] agents, Agent sender) {
        Position senderPos = sender.getPosition();
        int senderID = sender.getId();
        ArrayList<Integer> receivers = new ArrayList<>();
        for (Agent agent : agents) {
            int id = agent.getId();
            if (id != senderID && this.manhattan.distance(senderPos, agent.getPosition()) < this.maxDistance) {
                receivers.add(id);
            }
        }
        return receivers;
    }

}
