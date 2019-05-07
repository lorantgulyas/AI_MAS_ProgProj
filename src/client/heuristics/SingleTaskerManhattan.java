package client.heuristics;

import client.distance.LazyManhattan;
import client.state.*;

public class SingleTaskerManhattan extends AbstractSingleTasker {
    public SingleTaskerManhattan(State initialState) {
        super(initialState, new LazyManhattan());
    }

    @Override
    public String toString() {
        return "single-tasker-manhattan";
    }
}
