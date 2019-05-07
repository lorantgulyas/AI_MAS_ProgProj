package client.heuristics;

import client.distance.LazyShortestPath;
import client.state.*;

public class SingleTaskerShortestPath extends AbstractSingleTasker {
    public SingleTaskerShortestPath(State initialState) {
        super(initialState, new LazyShortestPath(initialState));
    }

    @Override
    public String toString() {
        return "single-tasker-shortest-path";
    }
}
