package client.config;

import client.definitions.ADistance;
import client.definitions.AHeuristic;
import client.heuristics.*;
import client.heuristics.unblocker.Unblocker;
import client.path.AllObjectsAStar;
import client.path.WallOnlyAStar;
import client.state.Goal;
import client.state.State;

import java.util.HashMap;

public class Heuristic {

    public static AHeuristic parseHeuristic(
            String heuristic,
            State initialState,
            ADistance distance,
            int stateSize,
            AllObjectsAStar allObjectsAStar,
            WallOnlyAStar wallOnlyAStar,
            HashMap<Goal, Integer> goalBoxMap
    )
            throws UnknownHeuristicException {
        switch (heuristic) {
            case "floodfill":
                return new Floodfill(initialState);
            case "goal-seeker":
                return new GoalSeeker(initialState, distance);
            case "manhattan":
                return new Manhattan(initialState);
            case "single-tasker":
                return new SingleTasker(initialState, distance, stateSize);
            case "unblocker":
                return new Unblocker(initialState, distance, allObjectsAStar, wallOnlyAStar, goalBoxMap);
            default:
                throw new UnknownHeuristicException();
        }
    }

}
