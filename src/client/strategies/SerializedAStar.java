package client.strategies;

import client.Solution;
import client.definitions.AHeuristic;
import client.definitions.AStrategy;
import client.graph.Plan;
import client.graph.PlanComparator;
import client.state.State;

import java.util.HashSet;
import java.util.PriorityQueue;

public class SerializedAStar extends AStrategy {

    public SerializedAStar(AHeuristic heuristic) {super(heuristic); }

    public Solution plan(State initialState) {

    }

    @Override
    public String toString() {
        return "Serialized A*";
    }

    class AStar {
        private HashSet<Plan> explored;
        private PriorityQueue<Plan> frontier;
        private HashSet<Plan> frontierSet;
        private AHeuristic heuristic;

        public AStar(AHeuristic heuristic, State initialState) {
            PlanComparator comparator = new PlanComparator();
            this.heuristic = heuristic;
            this.explored = new HashSet<>();
            this.frontier = new PriorityQueue<>(comparator);
            this.frontierSet = new HashSet<>();
            Plan root = new Plan(initialState);
            this.addToFrontier(root);
        }

        private Plan getAndRemoveLeaf() {
            Plan plan = frontier.poll();
            frontierSet.remove(plan);
            return plan;
        }

        private void addToExplored(Plan n) {
            this.explored.add(n);
        }

        private void addToFrontier(Plan n) {
            frontier.add(n);
            frontierSet.add(n);
        }

        private boolean isExplored(Plan n) {
            return this.explored.contains(n);
        }

        private boolean frontierIsEmpty() {
            return frontier.isEmpty();
        }

        private boolean inFrontier(Plan n) {
            return frontierSet.contains(n);
        }


    }
}
