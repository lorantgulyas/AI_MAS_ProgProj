package client.config;

import client.definitions.*;
import client.distance.LazyManhattan;
import client.distance.LazyShortestPath;
import client.distance.ShortestUnblockedPath;
import client.path.AllObjectsAStar;
import client.path.WallOnlyAStar;
import client.state.State;

public class Distance {

    public static ADistance parseDistamce(
            String distance,
            State initialState,
            int stateSize,
            AllObjectsAStar allObjectsAStar,
            WallOnlyAStar wallOnlyAStar
    )
            throws UnknownDistanceException {
        switch (distance) {
            case "manhattan":
                return new LazyManhattan();
            case "shortest-path":
                return new LazyShortestPath(initialState, stateSize, wallOnlyAStar);
            case "shortest-unblocked-path":
                return new ShortestUnblockedPath(initialState, stateSize, allObjectsAStar);
            default:
                throw new UnknownDistanceException();
        }
    }

}
