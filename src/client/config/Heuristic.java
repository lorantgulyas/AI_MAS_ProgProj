package client.config;

public enum Heuristic {

    MANHATTAN;

    public static Heuristic parseHeuristic(String heuristic) throws UnknownHeuristicException {
        switch (heuristic) {
            case "manhattan":
                return MANHATTAN;
            default:
                throw new UnknownHeuristicException();
        }
    }

}
