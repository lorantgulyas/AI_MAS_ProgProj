package client.config;

import client.definitions.AHeuristic;
import client.definitions.AMessagePolicy;
import client.definitions.AStrategy;
import client.strategies.CooperativeAStar;
import client.strategies.MultiAgentAStar;

public class Strategy {

    public static AStrategy parseStrategy(String strategy, AHeuristic heuristic, AMessagePolicy messagePolicy)
            throws UnknownStrategyException {
        switch (strategy) {
            case "cooperative_astar":
                return new CooperativeAStar(heuristic);
            case "multi-agent_astar":
                return new MultiAgentAStar(heuristic, messagePolicy);
            default:
                throw new UnknownStrategyException();
        }
    }

}
