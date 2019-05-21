package client.config;

import client.definitions.ADistance;
import client.definitions.AHeuristic;
import client.heuristics.*;
import client.heuristics.unblocker.Unblocker;
import client.state.State;

public class Heuristic {

    public static AHeuristic parseHeuristic(String heuristic, State initialState, ADistance distance, int stateSize)
            throws UnknownHeuristicException {
        switch (heuristic) {
            case "floodfill":
                return new Floodfill(initialState);
            case "manhattan":
                return new Manhattan(initialState);
            case "single-tasker":
                return new SingleTasker(initialState, distance, stateSize);
            case "unblocker":
                return new Unblocker(initialState, distance, stateSize);
            default:
                throw new UnknownHeuristicException();
        }
    }

}
