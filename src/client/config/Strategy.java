package client.config;

import client.definitions.AHeuristic;
import client.definitions.AMerger;
import client.definitions.AMessagePolicy;
import client.definitions.AStrategy;
import client.strategies.CooperativeAStar;
import client.strategies.MultiBodyAStar;
import client.strategies.multi_agent_astar.MultiAgentAStar;

public class Strategy {

    public static AStrategy parseStrategy(
            String strategy,
            AHeuristic heuristic,
            AMessagePolicy messagePolicy,
            AMerger merger,
            int[] agentIDMap
    ) throws UnknownStrategyException {
        switch (strategy) {
            case "cooperative_astar":
                return new CooperativeAStar(heuristic);
            case "multi-agent_astar":
                return new MultiAgentAStar(heuristic, messagePolicy, merger, agentIDMap);
            case "multi-body_astar":
                return new MultiBodyAStar(heuristic);
            default:
                throw new UnknownStrategyException();
        }
    }

}
