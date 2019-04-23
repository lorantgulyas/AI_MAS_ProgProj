package client.config;

import client.definitions.AHeuristic;
import client.definitions.AStrategy;
import client.strategies.CooperativeAStar;
import client.strategies.SerializedAStar;

public class Strategy {

    public static AStrategy parseStrategy(String strategy, AHeuristic heuristic) throws UnknownStrategyException {
        switch (strategy) {
            case "cooperative_astar":
                return new CooperativeAStar(heuristic);
            case "serialized_astar":
                return new SerializedAStar(heuristic);
            default:
                throw new UnknownStrategyException();
        }
    }

}
