package client.config;

public enum Algorithm {

    COOPERATIVE_ASTAR;

    public static Algorithm parseAlgorithm(String algorithm) {
        switch (algorithm) {
            case "cooperative_astar":
                return COOPERATIVE_ASTAR;
            default:
                return COOPERATIVE_ASTAR;
        }
    }
}
