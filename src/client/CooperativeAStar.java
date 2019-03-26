package client;

import client.definitions.AHeuristic;
import client.definitions.AState;
import client.definitions.AStrategy;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.PriorityQueue;

public class CooperativeAStar extends AStrategy {

    private HashSet<Timestamp> reservedCells;

    public CooperativeAStar(AHeuristic heuristic) {
        super(heuristic);
        this.reservedCells = new HashSet<>();
    }

    public Command[][] plan(AState initialState) {
        ArrayList<Command[]> plans = new ArrayList<>();
        Agent[] agents = initialState.getAgents();
        for (Agent agent : agents) {
            AStar astar = new AStar(this.heuristic, initialState, this.reservedCells);
            Action[] plan = astar.plan();
            ArrayList<Command> commands = new ArrayList<>() ;
            for (Action action : plan) {
                commands.add(action.getCommand());
                this.reservedCells.add(action.getTimestamp());
            }
            plans.add((Command[]) commands.toArray());
        }

        // find longest plan
        int maxLength = Integer.MIN_VALUE;
        for (Command[] plan : plans) {
            if (maxLength < plan.length) {
                maxLength = plan.length;
            }
        }

        // extend plans with NoOps
        ArrayList<Command[]> jointActions = new ArrayList<>();
        for (int i = 0; i < maxLength; i++) {
            ArrayList<Command> jointAction = new ArrayList<>();
            for (Command[] plan : plans) {
                if (i < plan.length) {
                    jointAction.add(plan[i]);
                } else {
                    jointAction.add(Command.NoOp);
                }
            }
            jointActions.add((Command[]) jointAction.toArray());
        }
        return (Command[][]) jointActions.toArray();
    }

    @Override
    public String toString() {
        return "Cooperative A*";
    }

    static class AStar {
        private AHeuristic heuristic;
        private HashSet<AState> explored;
        private PriorityQueue<AState> frontier;
        private HashSet<AState> frontierSet;
        private HashSet<Timestamp> reservedCells;

        public AStar(AHeuristic heuristic, AState state, HashSet<Timestamp> reservedCells) {
            super();
            this.explored = new HashSet<>();
            this.heuristic = heuristic;
            this.frontier = new PriorityQueue<AState>();
            this.frontierSet = new HashSet<>();
            this.reservedCells = reservedCells;
            this.addToFrontier(state);
        }

        public AState getAndRemoveLeaf() {
            AState n = frontier.poll();
            frontierSet.remove(n);
            return n;
        }

        public void addToExplored(AState n) {
            this.explored.add(n);
        }

        public void addToFrontier(AState n) {
            frontier.add(n);
            frontierSet.add(n);
        }

        public boolean isExplored(AState n) {
            return this.explored.contains(n);
        }

        public boolean frontierIsEmpty() {
            return frontier.isEmpty();
        }

        public boolean inFrontier(AState n) {
            return frontierSet.contains(n);
        }

        public Action[] plan() {
            while (true) {
                if (this.frontierIsEmpty()) {
                    return null;
                }

                AState leafState = this.getAndRemoveLeaf();

                if (leafState.isGoalState()) {
                    return leafState.extractPlan();
                }

                this.addToExplored(leafState);
                for (AState n : leafState.getExpandedStates()) {
                    if (!this.isExplored(n) && !this.inFrontier(n)) {
                        this.addToFrontier(n);
                    }
                }
            }
        }
    }
}
