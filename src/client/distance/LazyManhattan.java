package client.distance;

import client.definitions.ADistance;
import client.state.Position;
import client.state.State;

public class LazyManhattan extends ADistance {

    public int distance(State state, Position p1, Position p2) {
        return Math.abs(p1.getRow() - p2.getRow()) + Math.abs(p1.getCol() - p2.getCol());
    }

    @Override
    public String toString() {
        return "manhattan";
    }

}
