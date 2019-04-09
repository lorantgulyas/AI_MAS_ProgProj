package client.distance;

import client.definitions.ADistance;
import client.state.Position;

public class LazyManhattan extends ADistance {

    public int distance(Position p1, Position p2) {
        return Math.abs(p1.getRow() - p2.getRow()) + Math.abs(p1.getCol() - p2.getCol());
    }

}
