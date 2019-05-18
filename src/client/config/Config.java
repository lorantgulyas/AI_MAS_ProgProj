package client.config;

import client.definitions.*;

public class Config {

    private AHeuristic heuristic;
    private AStrategy strategy;
    private AMessagePolicy messagePolicy;
    private AMerger merger;
    private ADistance distance;

    public Config(
            AStrategy strategy,
            AHeuristic heuristic,
            AMessagePolicy messagePolicy,
            AMerger merger,
            ADistance distance
    ) {
        this.heuristic = heuristic;
        this.strategy = strategy;
        this.messagePolicy = messagePolicy;
        this.merger = merger;
        this.distance = distance;
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

    public AMerger getMerger() {
        return this.merger;
    }

    public ADistance getDistance() {
        return distance;
    }
}
