package client.strategies.multi_agent_astar;

import client.definitions.AHeuristic;
import client.graph.Plan;
import client.graph.PlanComparator;
import client.strategies.multi_agent_astar.messages.Message;
import client.strategies.multi_agent_astar.messages.SendNode;

import java.util.*;

public class ThreadedAgent extends Thread {

    private int agentID;
    private Channel channel;
    private AHeuristic heuristic;

    private HashMap<Plan, Plan> explored;
    private PriorityQueue<Plan> frontier;
    private HashMap<Plan, Plan> frontierSet;

    private Result result;

    public ThreadedAgent(int agentID, AHeuristic heuristic, client.state.State initialState) {
        this.agentID = agentID;
        this.channel = new Channel(agentID);
        this.heuristic = heuristic;
        PlanComparator comparator = new PlanComparator();
        this.explored = new HashMap<>();
        this.frontier = new PriorityQueue<>(comparator);
        this.frontierSet = new HashMap<>();
        Plan root = new Plan(initialState);
        this.addToFrontier(root);
    }

    public int getAgentID() {
        return this.agentID;
    }

    public Channel getChannel() {
        return this.channel;
    }

    /**
     * Assumes that the agents also contain the agent itself.
     *
     * @param agents
     */
    public void setOtherAgents(ThreadedAgent[] agents) {
        this.channel.setChannels(agents);
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

    private void addToExplored(Plan node) {
        this.explored.put(node, node);
    }

    private void addToFrontier(Plan node) {
        this.frontier.add(node);
        this.frontierSet.put(node, node);
    }

    private void explored2frontier(Plan node) {
        this.explored.remove(node);
        this.addToFrontier(node);
    }

    private void explored2frontierAndReplace(Plan previous, Plan next) {
        this.explored.remove(previous);
        this.replaceInFrontier(previous, next);
    }

    private boolean frontierIsEmpty() {
        return this.frontier.isEmpty();
    }

    private void replaceInFrontier(Plan previous, Plan next) {
        this.frontier.remove(previous);
        this.frontier.add(next);
        this.frontierSet.replace(previous, next);
    }

    private void removeFromExplored(Plan node) {
        this.explored.remove(node);
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
            boolean noSolutionExists = this.verifyNoSolutionExists(frontierIsEmpty);
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
        ArrayList<Message> messages = this.channel.deliverAll();
        for (Message message : messages) {
            if (message instanceof SendNode) {
                SendNode nodeMessage = (SendNode) message;
                this.processMessage(nodeMessage.getNode());
            }
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
                    this.removeFromExplored(fromExplored);
                }
            } else {
                // same g-value in explored and frontier
                // so we don't need to explore it again
                this.removeFromFrontier(message);
            }
        }
    }

    private boolean expand() {
        Plan leaf = this.getAndRemoveLeaf();
        this.addToExplored(leaf);

        boolean isOptimalSolution = this.verifyIsOptimalSolution(leaf);
        if (isOptimalSolution) {
            return true;
        }

        // TODO: only actions that are public to other agents should be sent in order to save on communication overhead
        this.channel.broadcast(leaf);

        ArrayList<Plan> successors = leaf.getChildren(this.heuristic, this.agentID);
        for (Plan successor : successors) {
            this.processSuccessor(successor);
        }

        return false;
    }

    private void processSuccessor(Plan successor) {
        Plan successorFromExplored = this.explored.get(successor);
        Plan successorFromFrontier = this.frontierSet.get(successor);
        boolean inExplored = successorFromExplored != null;
        boolean inFrontier = successorFromFrontier != null;
        if (!inExplored && !inFrontier) {
            this.addToFrontier(successor);
        } else if (inExplored && !inFrontier) {
            if (successor.f() < successorFromExplored.f()) {
                this.explored2frontier(successor);
            }
        } else if (!inExplored && inFrontier) {
            if (successor.f() < successorFromFrontier.f()) {
                this.replaceInFrontier(successorFromFrontier, successor);
            }
        } else {
            if (successorFromExplored.f() < successorFromFrontier.f()) {
                if (successor.f() < successorFromExplored.f()) {
                    this.explored2frontierAndReplace(successorFromFrontier, successor);
                } else {
                    // same f-value in explored and frontier
                    // so we don't need to explore it again
                    this.removeFromFrontier(successor);
                }
            } else if (successorFromFrontier.f() < successorFromExplored.f()) {
                if (successor.f() < successorFromFrontier.f()) {
                    this.explored2frontierAndReplace(successorFromFrontier, successor);
                } else {
                    this.removeFromExplored(successor);
                }
            } else {
                // same f-value in explored and frontier
                // so we don't need to explore it again
                this.removeFromFrontier(successor);
            }
        }
    }

    private boolean verifyIsOptimalSolution(Plan node) {
        // TODO: verify that this is an optimal solution (we might not be done yet)
        if (node.getState().isGoalState()) {
            this.channel.broadcast(node);
            long explored = this.explored.size();
            long generated = explored + this.frontier.size();
            this.result = new Result(node.extract(), explored, generated);
            return true;
        }
        return false;
    }

    private boolean verifyNoSolutionExists(boolean frontierIsEmpty) {
        // TODO: verify that no solution exists (we might not be done yet)
        //if (frontierIsEmpty && this.otherAgentsFrontierIsEmpty() && this.messageQueueIsEmpty() && this.otherAgentsMessageQueueIsEmpty()) {
        if (false) {
            long explored = this.explored.size();
            long generated = explored + this.frontier.size();
            this.result = new Result(null, explored, generated);
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "Agent " + this.agentID;
    }
}
