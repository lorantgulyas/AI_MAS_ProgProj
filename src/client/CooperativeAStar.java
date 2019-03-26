package client;

import client.definitions.AHeuristic;
import client.definitions.AStrategy;

import java.util.ArrayList;

public class CooperativeAStar extends AStrategy {

    public CooperativeAStar(AHeuristic heuristic) {
        super(heuristic);
        //for multiple agents instantiation
        //this.agent...
        //setup position?
        //setup color?
        //setup number?
    }

    //To have the general search method such that each agent can search on their own.
    public ArrayList<State> Search(AStrategy strategy) {
        System.err.format("Search starting with strategy %s.\n", strategy.toString()); //for this agent
        State state = new State(null, 0, 0); //initial state
        strategy.addToFrontier(state);

        int iterations = 0;
        while (true) {
            if (iterations == 1000) {
                System.err.println(strategy.searchStatus());
                iterations = 0;
            }

            if (strategy.frontierIsEmpty()) {
                return null;
            }

            State leafState = strategy.getAndRemoveLeaf();

            if (leafState.isGoalState()) {
                return leafState.extractPlan();
            }

            strategy.addToExplored(leafState);
            for (State n : leafState.getExpandedStates()) { // The list of expanded states is shuffled randomly; see State.java.
                if (!strategy.isExplored(n) && !strategy.inFrontier(n)) {
                    strategy.addToFrontier(n);
                }
            }
            iterations++;
        }
    }
}
