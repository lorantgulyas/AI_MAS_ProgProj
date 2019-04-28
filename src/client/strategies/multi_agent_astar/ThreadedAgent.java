package client.strategies.multi_agent_astar;

import client.definitions.AHeuristic;
import client.graph.Plan;
import client.graph.PlanComparator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentLinkedDeque;

public class ThreadedAgent extends Thread {

    private int agentID;

    private ConcurrentLinkedDeque<Plan> messageQueue;
    private ThreadedAgent[] otherAgents;

    private HashMap<Plan, Plan> explored;
    private PriorityQueue<Plan> frontier;
    private HashMap<Plan, Plan> frontierSet;
    private AHeuristic heuristic;

    private Result result;

    public ThreadedAgent(int agentID, AHeuristic heuristic, client.state.State initialState) {
        this.agentID = agentID;
        this.messageQueue = new ConcurrentLinkedDeque<>();
        PlanComparator comparator = new PlanComparator();
        this.heuristic = heuristic;
        this.explored = new HashMap<>();
        this.frontier = new PriorityQueue<>(comparator);
        this.frontierSet = new HashMap<>();
        Plan root = new Plan(initialState);
        this.addToFrontier(root);
    }

    private Plan getAndRemoveLeaf() {
        Plan plan = this.frontier.poll();
        this.frontierSet.remove(plan);
        return plan;
    }

    private void addToExplored(Plan n) {
        this.explored.put(n, n);
    }

    private void addToFrontier(Plan n) {
        this.frontier.add(n);
        this.frontierSet.put(n, n);
    }

    private void replaceInFrontier(Plan previous, Plan next) {
        this.frontier.remove(previous);
        this.frontier.add(next);
        if (this.frontierSet.containsKey(previous)) {
            this.frontierSet.replace(previous, next);
        } else {
            System.err.println("NO!");
            this.frontierSet.put(previous, next);
        }
    }

    public boolean frontierIsEmpty() {
        return this.frontier.isEmpty();
    }

    public int getAgentID() {
        return this.agentID;
    }

    public void addMessage(Plan message) {
        this.messageQueue.add(message);
    }

    /**
     * Assumes that the agents also contain the agent itself.
     *
     * @param agents
     */
    public void setOtherAgents(ThreadedAgent[] agents) {
        ThreadedAgent[] otherAgents = new ThreadedAgent[agents.length - 1];
        int j = 0;
        for (int i = 0; i < agents.length; i++) {
            if (i != this.agentID) {
                otherAgents[j] = agents[i];
                j++;
            }
        }
        this.otherAgents = otherAgents;
    }

    public Result getResult() {
        return this.result;
    }

    public boolean isDone() {
        return this.result != null;
    }

    public void run() {
        long i = 0;
        while (true) {
            if (i % 1 == 0) {
                System.err.println("Agent " + this.agentID + ": " + i);
            }

            // process messages
            while (!this.messageQueue.isEmpty()) {
                Plan message = this.messageQueue.poll();
                this.processMessage(message);
                // TODO: remove
                this.checkFrontiers();
            }

            // check for solution
            // TODO: verify that no solution exists (we might not be done yet)
            boolean frontierIsEmpty = this.frontierIsEmpty();
            if (frontierIsEmpty && this.otherAgentsFrontierIsEmpty()) {
                long explored = this.explored.size();
                long generated = explored + this.frontier.size();
                this.result = new Result(null, explored, generated);
                //System.err.println("Agent " + this.agentID + ": No solution found.");
                break;
            }

            if (frontierIsEmpty) {
                //System.err.println("Agent " + this.agentID + ": Frontier is empty.");
                continue;
            }

            // expand (explore)
            Plan leaf = this.getAndRemoveLeaf();
            this.addToExplored(leaf);

            //System.err.println("Agent " + this.agentID + ": Not empty.");

            // TODO: verify that this is an optimal solution (we might not be done yet)
            if (leaf.getState().isGoalState()) {
                this.broadcast(leaf);
                long explored = this.explored.size();
                long generated = explored + this.frontier.size();
                this.result = new Result(leaf.extract(), explored, generated);
                //System.err.println("Agent " + this.agentID + ": Is goal state.");
                break;
            }

            // TODO: only actions that are public to other agents should be sent in order to save on communication overhead
            this.broadcast(leaf);

            ArrayList<Plan> children = leaf.getChildren(this.heuristic, this.agentID);
            for (Plan child : children) {
                //if (this.explored.containsKey(child) && (this.frontier.contains(child) || this.frontierSet.containsKey(child))) {
                //    System.err.println("Agent " + this.agentID + ": Explored and frontier contains child!");
                //}
                if (this.frontier.contains(child) && !this.frontierSet.containsKey(child)) {
                    System.err.println("Agent " + this.agentID + ": Frontier but not frontierSet!");
                }

                Plan childFromExplored = this.explored.get(child);
                Plan childFromFrontier = this.frontierSet.get(child);
                boolean inExplored = childFromExplored != null;
                boolean inFrontier = childFromFrontier != null;
                if (!inExplored && !inFrontier) {
                    this.addToFrontier(child);
                } else if (inExplored && !inFrontier) {
                    if (child.f() < childFromExplored.f()) {
                        this.explored.remove(child);
                        this.addToFrontier(child);
                    }
                } else if (!inExplored && inFrontier) {
                    if (child.f() < childFromFrontier.f()) {
                        this.replaceInFrontier(childFromFrontier, child);
                    }
                } else {
                    if (childFromExplored.f() < childFromFrontier.f()) {
                        if (child.f() < childFromExplored.f()) {
                            this.explored.remove(child);
                            this.replaceInFrontier(childFromFrontier, child);
                        } else {
                            // same f-value in explored and frontier
                            // so we don't need to explore it again
                            this.frontierSet.remove(child);
                            this.frontier.remove(child);
                        }
                    } else if (childFromFrontier.f() < childFromExplored.f()) {
                        if (child.f() < childFromFrontier.f()) {
                            this.explored.remove(child);
                            this.replaceInFrontier(childFromFrontier, child);
                        } else {
                            this.explored.remove(child);
                        }
                    } else {
                        // same f-value in explored and frontier
                        // so we don't need to explore it again
                        this.frontierSet.remove(child);
                        this.frontier.remove(child);
                    }
                }

                /*
                Plan childFromExplored = this.explored.get(child);
                if (childFromExplored == null || child.f() < childFromExplored.f()) {
                    //if (this.frontier.contains(child)) {
                    //    System.err.println("Agent " + this.agentID + ": Frontier contains child!");
                    //}
                    Plan childFromFrontier = this.frontierSet.get(child);
                    if (childFromFrontier == null) {
                        this.addToFrontier(child);
                    } else if (child.f() < childFromFrontier.f()) {
                        this.replaceInFrontier(childFromFrontier, child);
                    } else {
                        this.addToFrontier(child);
                    }

                    if (childFromExplored != null) {
                        this.explored.remove(childFromExplored);
                    }
                }
                */

                // TODO: remove
                this.checkFrontiers();
            }
            i++;
        }
    }

    private void checkFrontiers() {
        for (Plan node : this.frontier) {
            if (!this.frontierSet.containsKey(node)) {
                System.err.println("BAD!");
            }
        }
    }

    private void processMessage(Plan message) {
        boolean addedToFrontier = false;
        Plan fromFrontier = this.frontierSet.get(message);
        if (fromFrontier != null && message.g() < fromFrontier.g()) {
            //if (this.frontier.contains(message)) {
            //    System.err.println("Agent " + this.agentID + ": Frontier contains message (frontier)!");
            //}
            this.replaceInFrontier(fromFrontier, message);
            addedToFrontier = true;
        }

        Plan fromExplored = this.explored.get(message);
        if (fromExplored != null && (message.g() < fromExplored.g() || addedToFrontier)) {
            this.explored.remove(fromExplored);
            //if (this.frontier.contains(message)) {
            //    System.err.println("Agent " + this.agentID + ": Frontier contains message (explored)!");
            //}
            if (!addedToFrontier) {
                this.addToFrontier(message);
            }
        }
    }

    private void broadcast(Plan message) {
        for (ThreadedAgent agent : this.otherAgents) {
            if (!agent.isDone()) {
                agent.addMessage(message);
            }
        }
    }

    private boolean otherAgentsFrontierIsEmpty() {
        for (ThreadedAgent agent : this.otherAgents) {
            if (!agent.frontierIsEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return "Agent " + this.agentID;
    }
}
