package client.strategies.multi_agent_astar;

import client.definitions.AHeuristic;
import client.graph.Plan;
import client.graph.PlanComparator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentLinkedDeque;

public class ThreadedAgent extends Thread {

    private int agentID;

    private ConcurrentLinkedDeque<Plan> messageQueue;
    private ConcurrentLinkedDeque<Plan>[] otherAgentsMessageQueues;

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
        Plan plan = frontier.poll();
        frontierSet.remove(plan);
        return plan;
    }

    private void addToExplored(Plan n) {
        this.explored.put(n, n);
    }

    private void addToFrontier(Plan n) {
        frontier.add(n);
        frontierSet.put(n, n);
    }

    private boolean isExplored(Plan n) {
        return this.explored.containsKey(n);
    }

    private boolean frontierIsEmpty() {
        return frontier.isEmpty();
    }

    private boolean inFrontier(Plan n) {
        return frontierSet.containsKey(n);
    }

    public int getAgentID() {
        return agentID;
    }

    public ConcurrentLinkedDeque<Plan> getMessageQueue() {
        return messageQueue;
    }

    /**
     * Assumes that the queues contains the agents own queue.
     *
     * @param messageQueues
     */
    public void setOtherAgentsMessageQueues(ConcurrentLinkedDeque<Plan>[] messageQueues) {
        ConcurrentLinkedDeque<Plan>[] otherQueues = new ConcurrentLinkedDeque[messageQueues.length - 1];
        int j = 0;
        for (int i = 0; i < messageQueues.length; i++) {
            if (i != this.agentID) {
                otherQueues[j] = messageQueues[i];
                j++;
            }
        }
        this.otherAgentsMessageQueues = otherQueues;
    }

    public Result getResult() {
        return this.result;
    }

    public void run() {
        while (true) {
            // check for solution
            // TODO: verify that no solution exists (we might not be done yet)
            if (this.frontierIsEmpty()) {
                long explored = this.explored.size();
                long generated = explored + this.frontier.size();
                this.result = new Result(null, explored, generated);
                break;
            }

            // process messages
            while (!this.messageQueue.isEmpty()) {
                Plan message = this.messageQueue.poll();
                this.processMessage(message);
            }

            // expand
            Plan leaf = this.getAndRemoveLeaf();
            this.addToExplored(leaf);

            // TODO: verify that this is an optimal solution (we might not be done yet)
            if (leaf.getState().agentIsDone(this.agentID)) {
                this.broadcast(leaf);
                long explored = this.explored.size();
                long generated = explored + this.frontier.size();
                this.result = new Result(leaf.extract(), explored, generated);
                break;
            }

            // TODO: only actions that are public to other agents should be sent in order to save on communication overhead
            this.broadcast(leaf);

            ArrayList<Plan> children = leaf.getChildren(this.heuristic, this.agentID);
            for (Plan child : children) {
                Plan childFromExplored = this.explored.get(child);
                if (childFromExplored == null || child.f() < childFromExplored.f()) {
                    this.addToFrontier(child);
                    if (childFromExplored != null) {
                        this.explored.remove(childFromExplored);
                    }
                }
            }
        }
    }

    private void processMessage(Plan message) {
        Plan fromFrontier = this.frontierSet.get(message);
        if (fromFrontier != null && fromFrontier.g() > message.g()) {
            this.frontier.remove(fromFrontier);
            this.frontier.add(message);
            this.frontierSet.replace(fromFrontier, message);
            return;
        }

        Plan fromExplored = this.explored.get(message);
        if (fromExplored != null && fromExplored.g() > message.g()) {
            this.explored.remove(fromExplored);
            this.addToFrontier(message);
            return;
        }
    }

    private void broadcast(Plan message) {
        for (ConcurrentLinkedDeque<Plan> queue : this.otherAgentsMessageQueues) {
            queue.add(message);
        }
    }

    @Override
    public String toString() {
        return "Agent " + this.agentID;
    }
}
