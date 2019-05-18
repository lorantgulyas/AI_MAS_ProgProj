package client.config;

import client.definitions.ADistance;
import client.definitions.AHeuristic;
import client.heuristics.*;
import client.state.State;

public class Heuristic {

    public static AHeuristic parseHeuristic(String heuristic, State initialState, ADistance distance)
            throws UnknownHeuristicException {
        switch (heuristic) {
            case "floodfill":
                return new Floodfill(initialState);
            case "manhattan":
                return new Manhattan(initialState);
            case "single-tasker":
                return new SingleTasker(initialState, distance);
            default:
                throw new UnknownHeuristicException();
        }
    }

}
