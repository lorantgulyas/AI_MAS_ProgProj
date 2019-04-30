package client.strategies.multi_agent_astar;

import client.definitions.AHeuristic;
import client.graph.Plan;
import client.graph.PlanComparator;

import java.util.*;
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

    public int getAgentID() {
        return this.agentID;
    }

    public void addMessage(Plan message) {
        this.messageQueue.add(message);
    }

    public boolean messageQueueIsEmpty() {
        return this.messageQueue.isEmpty();
    }

    public boolean frontierIsEmpty() {
        return this.frontier.isEmpty();
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
        this.frontierSet.replace(previous, next);
    }

    private void explored2frontier(Plan node) {
        this.explored.remove(node);
        this.addToFrontier(node);
    }

    private void explored2frontierAndReplace(Plan previous, Plan next) {
        this.explored.remove(previous);
        this.replaceInFrontier(previous, next);
    }

    private void removeFromFrontier(Plan node) {
        this.frontierSet.remove(node);
        this.frontier.remove(node);
    }

    public void run() {
        long i = 0;
        while (true) {
            if (i % 50000 == 0) {
                System.err.println("Agent " + this.agentID + ": " + i);
            }
            this.processMessages();
            boolean frontierIsEmpty = this.frontierIsEmpty();
            boolean noSolutionExists = this.noSolutionExists(frontierIsEmpty);
            if (noSolutionExists) {
                System.err.println("Agent " + this.agentID + ": No solution found.");
                break;
            }
            if (frontierIsEmpty) {
                continue;
            }
            boolean solved = this.expand();
            if (solved) {
                System.err.println("Agent " + this.agentID + ": Is goal state.");
                break;
            }
            i++;
        }
    }

    private void processMessages() {
        while (!this.messageQueue.isEmpty()) {
            Plan message = this.messageQueue.poll();
            this.processMessage(message);
        }
    }

    private void processMessage(Plan message) {
        Plan fromFrontier = this.frontierSet.get(message);
        Plan fromExplored = this.explored.get(message);
        if (fromFrontier == null && fromExplored == null) {
            this.addToFrontier(message);
        } else if (fromFrontier == null && fromExplored != null) {
            if (message.g() < fromExplored.g()) {
                this.explored2frontier(message);
            }
        } else if (fromFrontier != null && fromExplored == null) {
            if (message.g() < fromFrontier.g()) {
                this.replaceInFrontier(fromFrontier, message);
            }
        } else {
            if (fromExplored.g() < fromFrontier.g()) {
                if (message.g() < fromExplored.g()) {
                    this.explored2frontierAndReplace(fromExplored, message);
                } else {
                    // same g-value in explored and frontier
                    // so we don't need to explore it again
                    this.removeFromFrontier(message);
                }
            } else if (fromFrontier.g() < fromExplored.g()) {
                if (message.g() < fromFrontier.f()) {
                    this.explored2frontierAndReplace(fromExplored, message);
                } else {
                    this.explored.remove(fromExplored);
                }
            } else {
                // same g-value in explored and frontier
                // so we don't need to explore it again
                this.removeFromFrontier(message);
            }
        }
    }

    private boolean noSolutionExists(boolean frontierIsEmpty) {
        // TODO: verify that no solution exists (we might not be done yet)
        if (frontierIsEmpty && this.otherAgentsFrontierIsEmpty() && this.messageQueueIsEmpty() && this.otherAgentsMessageQueueIsEmpty()) {
            long explored = this.explored.size();
            long generated = explored + this.frontier.size();
            this.result = new Result(null, explored, generated);
            return true;
        }
        return false;
    }

    private boolean expand() {
        Plan leaf = this.getAndRemoveLeaf();
        this.addToExplored(leaf);

        // TODO: verify that this is an optimal solution (we might not be done yet)
        if (leaf.getState().isGoalState()) {
            this.broadcast(leaf);
            long explored = this.explored.size();
            long generated = explored + this.frontier.size();
            this.result = new Result(leaf.extract(), explored, generated);
            return true;
        }

        // TODO: only actions that are public to other agents should be sent in order to save on communication overhead
        this.broadcast(leaf);

        ArrayList<Plan> children = leaf.getChildren(this.heuristic, this.agentID);
        for (Plan child : children) {
            this.processChild(child);
        }

        return false;
    }

    private void processChild(Plan child) {
        Plan childFromExplored = this.explored.get(child);
        Plan childFromFrontier = this.frontierSet.get(child);
        boolean inExplored = childFromExplored != null;
        boolean inFrontier = childFromFrontier != null;
        if (!inExplored && !inFrontier) {
            this.addToFrontier(child);
        } else if (inExplored && !inFrontier) {
            if (child.f() < childFromExplored.f()) {
                this.explored2frontier(child);
            }
        } else if (!inExplored && inFrontier) {
            if (child.f() < childFromFrontier.f()) {
                this.replaceInFrontier(childFromFrontier, child);
            }
        } else {
            if (childFromExplored.f() < childFromFrontier.f()) {
                if (child.f() < childFromExplored.f()) {
                    this.explored2frontierAndReplace(childFromFrontier, child);
                } else {
                    // same f-value in explored and frontier
                    // so we don't need to explore it again
                    this.removeFromFrontier(child);
                }
            } else if (childFromFrontier.f() < childFromExplored.f()) {
                if (child.f() < childFromFrontier.f()) {
                    this.explored2frontierAndReplace(childFromFrontier, child);
                } else {
                    this.explored.remove(child);
                }
            } else {
                // same f-value in explored and frontier
                // so we don't need to explore it again
                this.removeFromFrontier(child);
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

    private boolean otherAgentsMessageQueueIsEmpty() {
        for (ThreadedAgent agent : this.otherAgents) {
            if (!agent.messageQueueIsEmpty()) {
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
