package client.strategies;

import client.PerformanceStats;
import client.Solution;
import client.definitions.AHeuristic;
import client.definitions.AStrategy;
import client.graph.*;
import client.state.State;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.PriorityQueue;

public class MultiBodyAStar extends AStrategy {

    private HashSet<JointPlan> explored;
    private PriorityQueue<JointPlan> frontier;
    private HashSet<JointPlan> frontierSet;
    private AHeuristic heuristic;

    public MultiBodyAStar(AHeuristic heuristic) {
        super(heuristic);
        this.heuristic = heuristic;
    }

    public Solution plan(State initialState) {
        PlanComparator comparator = new PlanComparator();
        this.explored = new HashSet<>();
        this.frontier = new PriorityQueue<>(comparator);
        this.frontierSet = new HashSet<>();
        JointPlan root = new JointPlan(initialState);
        this.addToFrontier(root);

        ArrayList<ArrayList<Action>> result = null;
        long i = 0;
        while (true) {
            if (i % 100 == 0) {
                System.err.println("Iteration: " + i);
            }
            if (this.frontierIsEmpty()) {
                break;
            }

            JointPlan leaf = this.getAndRemoveLeaf();

            if (leaf.getState().isGoalState()) {
                result = leaf.extract();
                break;
            }

            this.addToExplored(leaf);
            ArrayList<JointPlan> children = leaf.getChildren(this.heuristic);
            for (JointPlan node : children) {
                if (!this.isExplored(node) && !this.inFrontier(node)) {
                    this.addToFrontier(node);
                }
            }
            i++;
        }

        long explored = this.explored.size();
        long generated = explored + this.frontier.size();
        PerformanceStats stats = new PerformanceStats( 0,  explored,  generated);

        Command[][] plan = result == null ? new Command[0][0] : this.jointActions2commmands(result);
        return new Solution(plan, stats);
    }

    @Override
    public String toString() {
        return "Multi-Body A*";
    }

    private JointPlan getAndRemoveLeaf() {
        JointPlan plan = frontier.poll();
        frontierSet.remove(plan);
        return plan;
    }

    private void addToExplored(JointPlan node) {
        this.explored.add(node);
    }

    private void addToFrontier(JointPlan node) {
        this.frontier.add(node);
        this.frontierSet.add(node);
    }

    private boolean isExplored(JointPlan node) {
        return this.explored.contains(node);
    }

    private boolean frontierIsEmpty() {
        return this.frontier.isEmpty();
    }

    private boolean inFrontier(JointPlan node) {
        return this.frontierSet.contains(node);
    }

    private Command[][] jointActions2commmands(ArrayList<ArrayList<Action>> jointActions) {
        int n = jointActions.size();
        int m = n == 0 ? 0 : jointActions.get(0).size();
        Command[][] plan = new Command[n][m];
        for (int i = 0; i < n; i++) {
            ArrayList<Action> jointAction = jointActions.get(i);
            for (int j = 0; j < m; j++) {
                Action action = jointAction.get(j);
                plan[i][j] = action.getCommand();
            }
        }
        return plan;
    }

}
