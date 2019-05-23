package client.strategies.multi_agent_astar;

import client.definitions.AHeuristic;
import client.definitions.AMessagePolicy;
import client.graph.Action;
import client.graph.Plan;
import client.graph.PlanComparator;
import client.strategies.multi_agent_astar.messages.*;

import java.util.*;

public class ThreadedAgent extends Thread {

    private int[] agentIDMap;
    private int agentID;
    private AHeuristic heuristic;
    private AMessagePolicy policy;

    private Channel channel;
    private HashMap<EmptyFrontierRequest, EmptyFrontierResponse[]> emptyFrontierSnapshots;
    private int nAgents;

    private boolean verifyingNoSolutionExists = false;

    private HashMap<Plan, Plan> explored;
    private PriorityQueue<Plan> frontier;
    private HashMap<Plan, Plan> frontierSet;

    private Terminator terminator;
    private Result result;

    public ThreadedAgent(
            int[] agentIDMap,
            int agentID,
            Terminator terminator,
            AHeuristic heuristic,
            AMessagePolicy policy,
            client.state.State initialState
    ) {
        super();
        this.agentIDMap = agentIDMap;
        this.agentID = agentID;
        this.heuristic = heuristic;
        this.policy = policy;
        this.terminator = terminator;
        this.channel = new Channel(agentID);
        this.emptyFrontierSnapshots = new HashMap<>();
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
        this.nAgents = agents.length;
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
        while (this.terminator.isAlive()) {
            if (i % 10000 == 0) {
                System.err.println("Agent " + this.agentIDMap[this.agentID] + ": " + i);
            }
            this.processMessages();
            boolean frontierIsEmpty = this.frontierIsEmpty();
            this.verifyNoSolutionExists(frontierIsEmpty);
            if (frontierIsEmpty) {
                continue;
            }
            this.expand();
            i++;
        }

        // wrap up by setting a result if it has not already been set
        if (this.result == null) {
            this.setResult(null);
        }
    }

    private void processMessages() {
        ArrayList<Message> messages = this.channel.deliverAll();
        for (Message message : messages) {
            if (message instanceof SendNode) {
                SendNode nodeMessage = (SendNode) message;
                this.processNode(nodeMessage.getNode());
            } else if (message instanceof EmptyFrontierRequest) {
                this.processEmptyFrontierRequest((EmptyFrontierRequest) message);
            } else if (message instanceof EmptyFrontierResponse) {
                this.processEmptyFrontierResponse((EmptyFrontierResponse) message);
            }
        }
    }

    private void processNode(Plan message) {
        Plan fromFrontier = this.frontierSet.get(message);
        Plan fromExplored = this.explored.get(message);
        if (fromFrontier == null && fromExplored == null) {
            this.addToFrontier(message);
        } else if (fromFrontier == null) {
            if (message.g() < fromExplored.g()) {
                this.explored2frontier(message);
            }
        } else if (fromExplored == null) {
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

    private void processEmptyFrontierRequest(EmptyFrontierRequest request) {
        boolean state = this.frontierIsEmpty();
        this.channel.sendEmptyFrontierResponse(request, state);
    }

    private void processEmptyFrontierResponse(EmptyFrontierResponse response) {
        EmptyFrontierRequest request = response.getRequest();
        EmptyFrontierResponse[] responses = this.emptyFrontierSnapshots.get(request);
        int fromAgentID = response.getAgentID();
        responses[fromAgentID] = response;

        // check if all responses have been received
        for (EmptyFrontierResponse r : responses) {
            if (r == null) {
                return;
            }
        }

        this.verifyingNoSolutionExists = false;

        // check if all frontiers are empty
        for (EmptyFrontierResponse r : responses) {
            if (!r.getState()) {
                return;
            }
        }

        // ensure that frontier is still empty and that channel is empty
        if (!this.frontierIsEmpty() || !channel.isEmpty())
            return;

        // all responses have been received and all frontiers are empty
        this.setResult(null);
        this.terminator.foundNoSolution();
        System.err.println("Agent " + this.agentIDMap[this.agentID] + ": No solution found.");
    }

    private void expand() {
        Plan leaf = this.getAndRemoveLeaf();
        this.addToExplored(leaf);

        boolean isSolution = this.verifyIsSolution(leaf);
        if (isSolution) {
            return;
        }

        Iterable<Integer> receivers = this.policy.receivers(leaf, this.agentID);
        this.channel.sendTo(receivers, leaf);

        ArrayList<Plan> successors = leaf.getChildren(this.heuristic, this.agentID);
        for (Plan successor : successors) {
            this.processSuccessor(successor);
        }
    }

    private void processSuccessor(Plan successor) {
        Plan successorFromExplored = this.explored.get(successor);
        Plan successorFromFrontier = this.frontierSet.get(successor);
        boolean inExplored = successorFromExplored != null;
        boolean inFrontier = successorFromFrontier != null;
        if (!inExplored && !inFrontier) {
            this.addToFrontier(successor);
        } else if (!inFrontier) {
            if (successor.f() < successorFromExplored.f()) {
                this.explored2frontier(successor);
            }
        } else if (!inExplored) {
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

    private boolean verifyIsSolution(Plan node) {
        // TODO: verify that this is an optimal solution (we might not have found an optimal solution yet)
        if (node.getState().isGoalState()) {
            this.setResult(node.extract());
            this.terminator.foundSolution();
            System.err.println("Agent " + this.agentIDMap[this.agentID] + ": Is goal state.");
            return true;
        }
        return false;
    }

    private void verifyNoSolutionExists(boolean frontierIsEmpty) {
        if (!this.verifyingNoSolutionExists && frontierIsEmpty) {
            EmptyFrontierRequest request = this.channel.makeEmptyFrontierRequest(frontierIsEmpty);
            EmptyFrontierResponse[] responses = new EmptyFrontierResponse[this.nAgents];
            responses[this.agentID] = new EmptyFrontierResponse(request, this.agentID, frontierIsEmpty);
            this.emptyFrontierSnapshots.put(request, responses);
            this.verifyingNoSolutionExists = true;
            this.channel.sendEmptyFrontierRequest(request);
        }
    }

    private void setResult(Action[] actions) {
        long messages = this.channel.getMessagesSent();
        long explored = this.explored.size();
        long generated = explored + this.frontier.size();
        this.result = new Result(actions, messages, explored, generated);
    }

    @Override
    public String toString() {
        return "Agent " + this.agentID;
    }
}
