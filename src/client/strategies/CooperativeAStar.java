package client.strategies;

import client.PerformanceStats;
import client.Solution;
import client.graph.Action;
import client.graph.Command;
import client.graph.Plan;
import client.graph.Timestamp;
import client.definitions.AHeuristic;
import client.definitions.AStrategy;
import client.state.Agent;
import client.state.State;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.PriorityQueue;

public class CooperativeAStar extends AStrategy {

    private HashSet<Timestamp> reservedCells;

    public CooperativeAStar(AHeuristic heuristic) {
        super(heuristic);
        this.reservedCells = new HashSet<>();
    }

    private Pair<ArrayList<Command[]>, Long> makePlans(State initialState) {
        ArrayList<Command[]> plans = new ArrayList<>();
        Agent[] agents = initialState.getAgents();
        long nodesExplored = 0;
        for (Agent agent : agents) {
            Plan root = new Plan(initialState, this.reservedCells);
            AStar astar = new AStar(agent.getId(), this.heuristic, root);
            Pair<Action[], Long> result = astar.plan();
            Action[] plan = result.first;
            nodesExplored += result.second;
            ArrayList<Command> commands = new ArrayList<>();
            // a plan may be null if no solution could be found for this agent
            if (plan != null) {
                for (Action action : plan) {
                    commands.add(action.getCommand());
                    Timestamp[] timestamps = action.getTimestamps();
                    for (Timestamp t : timestamps) {
                        this.reservedCells.add(t);
                    }
                };
            }
            plans.add(commands.toArray(new Command[0]));
        }
        return new Pair(plans, nodesExplored);
    }

    private int findMaxPlanLength(ArrayList<Command[]> plans) {
        int maxLength = Integer.MIN_VALUE;
        for (Command[] plan : plans) {
            if (maxLength < plan.length) {
                maxLength = plan.length;
            }
        }
        return maxLength;
    }

    private Command[][] extendPlans(ArrayList<Command[]> plans) {
        // extend plans with NoOps
        int maxLength = findMaxPlanLength(plans);
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
            jointActions.add(jointAction.toArray(new Command[0]));
        }
        return jointActions.toArray(new Command[0][0]);
    }

    public Solution plan(State initialState) {
        long startTime = System.currentTimeMillis();
        Pair<ArrayList<Command[]>, Long> result = this.makePlans(initialState);
        Command[][] plan = this.extendPlans(result.first);
        double memoryUsed = this.memoryUsed();
        double timeSpent = this.timeSpent(startTime);
        PerformanceStats stats = new PerformanceStats(memoryUsed, result.second, plan.length, timeSpent);
        return new Solution(plan, stats);
    }

    @Override
    public String toString() {
        return "Cooperative A*";
    }

    class AStar {
        private int agentId;
        private HashSet<Plan> explored;
        private PriorityQueue<Plan> frontier;
        private HashSet<Plan> frontierSet;

        public AStar(int agentId, AHeuristic heuristic, Plan plan) {
            this.agentId = agentId;
            this.explored = new HashSet<>();
            this.frontier = new PriorityQueue<>(heuristic);
            this.frontierSet = new HashSet<>();
            this.addToFrontier(plan);
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

        public Pair<Action[], Long> plan() {
            long i = 0;
            while (true) {
                if (i % 10000 == 0) {
                    System.err.println("Agent " + this.agentId + ": " + i);
                }
                if (this.frontierIsEmpty()) {
                    return new Pair(null, i);
                }

                Plan leaf = this.getAndRemoveLeaf();

                if (leaf.getState().agentIsDone(this.agentId)) {
                    return new Pair(leaf.extract(), i);
                }

                this.addToExplored(leaf);
                for (Plan node : leaf.getChildren(this.agentId)) {
                    if (!this.isExplored(node) && !this.inFrontier(node)) {
                        this.addToFrontier(node);
                    }
                }
                i++;
            }
        }
    }

    class Pair<T1, T2> {
        public T1 first;
        public T2 second;

        public Pair(T1 first, T2 second) {
            this.first = first;
            this.second = second;
        }
    }
}
