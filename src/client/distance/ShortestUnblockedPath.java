package client.distance;

import client.definitions.ADistance;
import client.path.AllObjectsAStar;
import client.path.Node;
import client.state.Agent;
import client.state.Position;
import client.state.State;

/**
 * Finds the length of the shortest unblocked path between two positions.
 * An unblocked path will have no other agents, boxes, or walls blocking it.
 * Note that a box is not considered to be blocking if the agent can move it.
 * Returns a multiple of the state size in case no unblocked path can be found.
 */
public class ShortestUnblockedPath extends ADistance {

    private int stateSize;
    private AllObjectsAStar planner;

    public ShortestUnblockedPath(State initialState, int stateSize, AllObjectsAStar planner) {
        this.stateSize = stateSize;
        this.planner = planner;
    }

    public int distance(State state, Position p1, Position p2) {
        Node result;
        if (state.agentAt(p1)) {
            Agent agent = state.getAgentAt(p1);
            result = this.planner.plan(state, agent.getPosition(), p2, agent.getColor());
        } else if (state.agentAt(p2)) {
            Agent agent = state.getAgentAt(p2);
            result = this.planner.plan(state, agent.getPosition(), p1, agent.getColor());
        } else {
            result = this.planner.plan(state, p1, p2);
        }
        return result == null ? 10 * this.stateSize : result.g();
    }

    @Override
    public String toString() {
        return "shortest-unblocked-path";
    }

}
