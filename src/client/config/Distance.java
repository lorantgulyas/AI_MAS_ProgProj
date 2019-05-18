package client.config;

import client.definitions.*;
import client.distance.LazyManhattan;
import client.distance.LazyShortestPath;
import client.state.State;

public class Distance {

    public static ADistance parseDistamce(String distance, State initialState) throws UnknownDistanceException {
        switch (distance) {
            case "manhattan":
                return new LazyManhattan();
            case "shortest-path":
                return new LazyShortestPath(initialState);
            default:
                throw new UnknownDistanceException();
        }
    }

}
