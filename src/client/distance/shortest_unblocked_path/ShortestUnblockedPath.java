package client.distance.shortest_unblocked_path;

import client.definitions.ADistance;
import client.distance.LazyShortestPath;
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
    private AStar planner;

    public ShortestUnblockedPath(State initialState, int stateSize) {
        this.stateSize = stateSize;
        this.planner = new AStar();
    }

    public int distance(State state, Position p1, Position p2) {
        Node result;
        if (state.agentAt(p1)) {
            result = this.planner.plan(state, state.getAgentAt(p1), p2);
        } else if (state.agentAt(p2)) {
            result = this.planner.plan(state, state.getAgentAt(p2), p1);
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
