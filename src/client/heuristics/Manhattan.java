package client.heuristics;

import client.definitions.AHeuristic;
import client.State;

public class Manhattan extends AHeuristic {

    public Manhattan(State initialState) {
        super(initialState);
    }

    @Override
    public int h(State state) {
        // TODO
        return 0;
    }
}
