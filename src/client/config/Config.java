package client.config;

import client.definitions.AHeuristic;
import client.definitions.AStrategy;

public class Config {

    private AHeuristic heuristic;
    private AStrategy strategy;

    public Config(
            AStrategy strategy,
            AHeuristic heuristic
    ) {
        this.heuristic = heuristic;
        this.strategy = strategy;
    }

    public AHeuristic getHeuristic() {
        return heuristic;
    }

    public AStrategy getStrategy() {
        return strategy;
    }
}
