package client.definitions;

import client.Solution;
import client.state.State;

public abstract class AStrategy {

    protected AHeuristic heuristic;

    public AStrategy(AHeuristic heuristic) {
        this.heuristic = heuristic;
    }

    public abstract Solution plan(State initialState);

}
