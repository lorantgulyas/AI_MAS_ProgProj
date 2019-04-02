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

    public Command[][] plan(State initialState) {
        ArrayList<Command[]> plans = new ArrayList<>();
        Agent[] agents = initialState.getAgents();
        for (Agent agent : agents) {
            Plan root = new Plan(initialState);
            AStar astar = new AStar(agent.getId(), this.heuristic, root, this.reservedCells);
            Action[] plan = astar.plan();
            ArrayList<Command> commands = new ArrayList<>() ;
            for (Action action : plan) {
                commands.add(action.getCommand());
                Timestamp[] timestamps = action.getTimestamps();
                for (Timestamp t : timestamps) {
                    this.reservedCells.add(t);
                }
            }
            plans.add(commands.toArray(new Command[0]));
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
            jointActions.add(jointAction.toArray(new Command[0]));
        }
        return jointActions.toArray(new Command[0][0]);
    }

    @Override
    public String toString() {
        return "Cooperative A*";
    }

    static class AStar {
        private int agentId;
        private AHeuristic heuristic;
        private HashSet<Plan> explored;
        private PriorityQueue<Plan> frontier;
        private HashSet<Plan> frontierSet;
        private HashSet<Timestamp> reservedCells;

        public AStar(int agentId, AHeuristic heuristic, Plan plan, HashSet<Timestamp> reservedCells) {
            this.agentId = agentId;
            this.explored = new HashSet<>();
            this.heuristic = heuristic;
            this.frontier = new PriorityQueue<Plan>(heuristic);
            this.frontierSet = new HashSet<>();
            this.reservedCells = reservedCells;
            this.addToFrontier(plan);
        }

        public Plan getAndRemoveLeaf() {
            Plan plan = frontier.poll();
            frontierSet.remove(plan);
            return plan;
        }

        public void addToExplored(Plan n) {
            this.explored.add(n);
        }

        public void addToFrontier(Plan n) {
            frontier.add(n);
            frontierSet.add(n);
        }

        public boolean isExplored(Plan n) {
            return this.explored.contains(n);
        }

        public boolean frontierIsEmpty() {
            return frontier.isEmpty();
        }

        public boolean inFrontier(Plan n) {
            return frontierSet.contains(n);
        }

        public Action[] plan() {
            int i = 0;
            while (true) {
                if (i++ % 1000 == 0) {
                    System.err.println(i);
                }
                if (this.frontierIsEmpty()) {
                    return null;
                }

                Plan leaf = this.getAndRemoveLeaf();

                if (leaf.getState().agentIsDone(this.agentId)) {
                    return leaf.extract();
                }

                this.addToExplored(leaf);
                for (Plan n : leaf.getChildren(this.agentId, this.reservedCells)) {
                    if (!this.isExplored(n) && !this.inFrontier(n)) {
                        this.addToFrontier(n);
                        //System.err.println(n.getAction().getCommand());
                    }
                }
            }
        }
    }
}
