package client.config;

public enum Strategy {

    COOPERATIVE_ASTAR;

    public static Strategy parseStrategy(String strategy) throws UnknownStrategyException {
        switch (strategy) {
            case "cooperative_astar":
                return COOPERATIVE_ASTAR;
            default:
                throw new UnknownStrategyException();
        }
    }
}
