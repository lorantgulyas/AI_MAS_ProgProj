package client.config;

import client.definitions.AHeuristic;
import client.definitions.AMessagePolicy;
import client.definitions.AStrategy;

public class Config {

    private AHeuristic heuristic;
    private AStrategy strategy;
    private AMessagePolicy messagePolicy;

    public Config(
            AStrategy strategy,
            AHeuristic heuristic,
            AMessagePolicy messagePolicy
    ) {
        this.heuristic = heuristic;
        this.strategy = strategy;
        this.messagePolicy = messagePolicy;
    }

    public AHeuristic getHeuristic() {
        return this.heuristic;
    }

    public AStrategy getStrategy() {
        return this.strategy;
    }

    public AMessagePolicy getMessagePolicy() {
        return this.messagePolicy;
    }
}
