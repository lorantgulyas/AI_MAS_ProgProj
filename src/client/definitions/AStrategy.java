package client.definitions;

import client.Command;
import client.State;

public abstract class AStrategy {

    protected AHeuristic heuristic;

    public AStrategy(AHeuristic heuristic) {
        this.heuristic = heuristic;
    }

    public abstract Command[][] plan(AState initialState);

}
