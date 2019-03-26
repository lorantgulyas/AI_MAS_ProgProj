package client.config;

public class Config {

    private Algorithm algorithm;
    private Heuristic heuristic;

    public Config(
            Algorithm algorithm,
            Heuristic heuristic
    ) {
        this.algorithm = algorithm;
        this.heuristic = heuristic;
    }

    public Algorithm getAlgorithm() {
        return algorithm;
    }

    public Heuristic getHeuristic() {
        return heuristic;
    }
}
