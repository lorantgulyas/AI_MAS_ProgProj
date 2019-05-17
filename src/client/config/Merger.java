package client.config;

import client.definitions.AHeuristic;
import client.definitions.AMerger;
import client.heuristics.Floodfill;
import client.heuristics.Manhattan;
import client.heuristics.SingleTaskerManhattan;
import client.heuristics.SingleTaskerShortestPath;
import client.mergers.CellsUsed;
import client.mergers.Greedy;
import client.mergers.NoMerge;
import client.state.State;

public class Merger {

    public static AMerger parseMerger(String merger) throws UnknownMergerException {
        switch (merger) {
            case "cells-used":
                return new CellsUsed();
            case "greedy":
                return new Greedy();
            case "no-merge":
                return new NoMerge();
            default:
                throw new UnknownMergerException();
        }
    }

}
