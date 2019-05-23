package client.config;

import client.definitions.*;
import client.distance.LazyManhattan;
import client.distance.LazyShortestPath;
import client.distance.shortest_unblocked_path.ShortestUnblockedPath;
import client.state.State;

public class Distance {

    public static ADistance parseDistamce(String distance, State initialState, int stateSize)
            throws UnknownDistanceException {
        switch (distance) {
            case "manhattan":
                return new LazyManhattan();
            case "shortest-path":
                return new LazyShortestPath(initialState);
            case "shortest-unblocked-path":
                return new ShortestUnblockedPath(initialState, stateSize);
            default:
                throw new UnknownDistanceException();
        }
    }

}
