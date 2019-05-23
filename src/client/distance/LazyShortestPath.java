package client.distance;

import client.definitions.ADistance;
import client.path.Node;
import client.path.WallOnlyAStar;
import client.state.Position;
import client.state.State;

public class LazyShortestPath extends ADistance {
    private WallOnlyAStar planner;
    private int stateSize;

    public LazyShortestPath(State state, int stateSize, WallOnlyAStar planner) {
        this.planner = planner;
        this.stateSize = stateSize;
    }

    public int distance(State state, Position p1, Position p2) {
        Node result = this.planner.plan(state, p1, p2);
        return result == null ? this.stateSize * 10 : result.g();
    }

    @Override
    public String toString() {
        return "shortest-path";
    }
}
