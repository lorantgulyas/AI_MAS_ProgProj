package client.config;

public class Config {

    private Heuristic heuristic;
    private Strategy strategy;

    public Config(
            Strategy strategy,
            Heuristic heuristic
    ) {
        this.heuristic = heuristic;
        this.strategy = strategy;
    }

    public Heuristic getHeuristic() {
        return heuristic;
    }

    public Strategy getStrategy() {
        return strategy;
    }
}
