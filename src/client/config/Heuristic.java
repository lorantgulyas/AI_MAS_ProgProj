package client.config;

import client.definitions.AHeuristic;
import client.heuristics.Floodfill;
import client.heuristics.Manhattan;
import client.heuristics.SingleTaskerManhattan;
import client.heuristics.SingleTaskerShortestPath;
import client.state.State;

public class Heuristic {

    public static AHeuristic parseHeuristic(String heuristic, State initialState) throws UnknownHeuristicException {
        switch (heuristic) {
            case "floodfill":
                return new Floodfill(initialState);
            case "manhattan":
                return new Manhattan(initialState);
            case "single-tasker-manhattan":
                return new SingleTaskerManhattan(initialState);
            case "single-tasker-shortest-path":
                return new SingleTaskerShortestPath(initialState);
            default:
                throw new UnknownHeuristicException();
        }
    }

}
