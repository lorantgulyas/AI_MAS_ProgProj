package client.definitions;

import client.Memory;
import client.Solution;
import client.state.State;

public abstract class AStrategy {

    protected AHeuristic heuristic;

    public AStrategy(AHeuristic heuristic) {
        this.heuristic = heuristic;
    }

    protected double memoryUsed() {
        return Memory.used();
    }

    protected double timeSpent(long startTime) {
        return (System.currentTimeMillis() - startTime) / 1000f;
    }

    public abstract Solution plan(State initialState);

}
