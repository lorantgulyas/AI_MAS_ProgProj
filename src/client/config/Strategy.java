package client.config;

import client.definitions.AHeuristic;
import client.definitions.AStrategy;
import client.strategies.CooperativeAStar;

public class Strategy {

    public static AStrategy parseStrategy(String strategy, AHeuristic heuristic) throws UnknownStrategyException {
        switch (strategy) {
            case "cooperative_astar":
                return new CooperativeAStar(heuristic);
            default:
                throw new UnknownStrategyException();
        }
    }

}
