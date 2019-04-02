package client.config;

public enum Heuristic {

    MANHATTAN, SINGLE_TASKER;

    public static Heuristic parseHeuristic(String heuristic) throws UnknownHeuristicException {
        switch (heuristic) {
            case "manhattan":
                return MANHATTAN;
            case "single-tasker":
                return SINGLE_TASKER;
            default:
                throw new UnknownHeuristicException();
        }
    }
}
