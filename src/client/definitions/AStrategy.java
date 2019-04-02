package client.definitions;

import client.graph.Command;
import client.state.State;

public abstract class AStrategy {

    protected AHeuristic heuristic;

    public AStrategy(AHeuristic heuristic) {
        this.heuristic = heuristic;
    }

    public abstract Command[][] plan(State initialState);

}
