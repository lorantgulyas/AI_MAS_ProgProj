package client.config;

public enum Heuristic {

    MANHATTAN;

    public static Heuristic parseHeuristic(String heuristic) {
        switch (heuristic) {
            case "manhattan":
                return MANHATTAN;
            default:
                return MANHATTAN;
        }
    }

}
