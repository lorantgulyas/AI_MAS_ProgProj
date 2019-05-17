package client.strategies;

import client.PerformanceStats;
import client.Solution;
import client.graph.*;
import client.definitions.AHeuristic;
import client.definitions.AStrategy;
import client.state.Agent;
import client.state.Position;
import client.state.State;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.Math;
import java.util.*;

public class CooperativeAStar extends AStrategy {

    public CooperativeAStar(AHeuristic heuristic) {
        super(heuristic);
    }

    @NotNull
    @Contract("_ -> new")
    private CooperativeAStarResult makePlans(@NotNull State initialState) {
        ArrayList<Action[]> plans = new ArrayList<>();
        Agent[] agents = initialState.getAgents();
        ArrayList<Set<Position>> cellsUsed = new ArrayList<>();
        ArrayList<ArrayList<Action>> previousActions = new ArrayList<>();
        long nodesExplored = 0;
        long nodesGenerated = 0;
        for (Agent agent : agents) {
            AStar astar = new AStar(this.heuristic, initialState);
            AStarResult result = astar.plan(agent.getId(), cellsUsed, previousActions);
            nodesExplored += result.nodesExplored;
            nodesGenerated += result.nodesGenerated;
            // a plan may be null if no solution could be found for this agent
            if (result.plan != null) {
                this.updateCellsUsed(cellsUsed, result.plan);
                this.updatePreviousActions(previousActions, result.plan);
            }
            Action[] actions = result.plan == null ? new Action[] {} : result.plan;
            plans.add(actions);
        }
        return new CooperativeAStarResult(plans, nodesExplored, nodesGenerated);
    }

    private void updateCellsUsed(@NotNull ArrayList<Set<Position>> cellsUsed, @NotNull Action[] plan) {
        // update reserved cells with cells in this plan
        int nCellsUsed = cellsUsed.size();
        int nPlan = plan.length;
        int nMin = Math.min(nCellsUsed, nPlan);
        for (int i = 0; i < nMin; i++) {
            cellsUsed.get(i).addAll(plan[i].getCellsUsed());
        }
        for (int i = nMin; i < nPlan; i++) {
            // clone cells used from action
            HashSet<Position> used = new HashSet<>(plan[i].getCellsUsed());
            cellsUsed.add(used);
        }
    }

    private void updatePreviousActions(@NotNull ArrayList<ArrayList<Action>> previousActions, @NotNull Action[] plan) {
        // update previous actions with actions in this plan
        int nPreviousActions = previousActions.size();
        int nPlan = plan.length;
        int nMin = Math.min(nPreviousActions , nPlan);
        for (int i = 0; i < nMin; i++) {
            previousActions.get(i).add(plan[i]);
        }
        for (int i = nMin; i < nPlan; i++) {
            ArrayList<Action> actions = new ArrayList<>();
            actions.add(plan[i]);
            previousActions.add(actions);
        }
    }

    @Contract(pure = true)
    private int findMaxPlanLength(@NotNull ArrayList<Action[]> plans) {
        int maxLength = Integer.MIN_VALUE;
        for (Action[] plan : plans) {
            if (maxLength < plan.length) {
                maxLength = plan.length;
            }
        }
        return maxLength;
    }

    // TODO: it is actually not correct to just extend with NoOps
    // since the agent may be blocking other agents
    @NotNull
    private Command[][] extendPlans(ArrayList<Action[]> plans) {
        // extend plans with NoOps
        int maxLength = findMaxPlanLength(plans);
        ArrayList<Command[]> jointActions = new ArrayList<>();
        for (int i = 0; i < maxLength; i++) {
            ArrayList<Command> jointAction = new ArrayList<>();
            for (Action[] actions : plans) {
                if (i < actions.length) {
                    jointAction.add(actions[i].getCommand());
                } else {
                    jointAction.add(Command.NoOp);
                }
            }
            jointActions.add(jointAction.toArray(new Command[0]));
        }
        return jointActions.toArray(new Command[0][0]);
    }

    public Solution plan(State initialState) {
        CooperativeAStarResult result = this.makePlans(initialState);
        Command[][] plan = this.extendPlans(result.plan);
        PerformanceStats stats = new PerformanceStats(0,  result.nodesExplored,  result.nodesGenerated);
        return new Solution(plan, stats);
    }

    @Override
    public String toString() {
        return "Cooperative A*";
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

        public AStarResult plan(int agentId, ArrayList<Set<Position>> cellsUsed, ArrayList<ArrayList<Action>> previousCommands) {
            long i = 0;
            while (true) {
                if (i % 10000 == 0) {
                    System.err.println("Agent " + agentId + ": " + i);
                }
                if (this.frontierIsEmpty()) {
                    long explored = this.explored.size();
                    long generated = explored + this.frontier.size();
                    return new AStarResult(null, explored, generated);
                }

                Plan leaf = this.getAndRemoveLeaf();

                if (leaf.getState().agentIsDone(agentId)) {
                    long explored = this.explored.size();
                    long generated = explored + this.frontier.size();
                    return new AStarResult(leaf.extract(), explored, generated);
                }

                this.addToExplored(leaf);
                ArrayList<Plan> children = leaf.getConstrainedChildren(this.heuristic, agentId, cellsUsed, previousCommands);
                for (Plan node : children) {
                    if (!this.isExplored(node) && !this.inFrontier(node)) {
                        this.addToFrontier(node);
                    }
                }
                i++;
            }
        }
    }

    class AStarResult {
        public Action[] plan;
        public long nodesExplored;
        public long nodesGenerated;

        public AStarResult(Action[] plan, long nodesExplored, long nodesGenerated) {
            this.plan = plan;
            this.nodesExplored = nodesExplored;
            this.nodesGenerated = nodesGenerated;
        }
    }

    class CooperativeAStarResult {
        public ArrayList<Action[]> plan;
        public long nodesExplored;
        public long nodesGenerated;

        public CooperativeAStarResult(ArrayList<Action[]> plan, long nodesExplored, long nodesGenerated) {
            this.plan = plan;
            this.nodesExplored = nodesExplored;
            this.nodesGenerated = nodesGenerated;
        }
    }
}
