package client.config;

import client.definitions.AHeuristic;
import client.heuristics.Floodfill;
import client.heuristics.Manhattan;
import client.heuristics.SingleTasker;
import client.state.State;

public class Heuristic {

    public static AHeuristic parseHeuristic(String heuristic, State initialState) throws UnknownHeuristicException {
        switch (heuristic) {
            case "floodfill":
                return new Floodfill(initialState);
            case "manhattan":
                return new Manhattan(initialState);
            case "single-tasker":
                return new SingleTasker(initialState);
            default:
                throw new UnknownHeuristicException();
        }
    }

}
