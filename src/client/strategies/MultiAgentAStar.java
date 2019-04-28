package client.strategies;

import client.PerformanceStats;
import client.Solution;
import client.definitions.AHeuristic;
import client.definitions.AStrategy;
import client.graph.Action;
import client.graph.Command;
import client.graph.Plan;
import client.state.Agent;
import client.state.State;
import client.strategies.multi_agent_astar.Result;
import client.strategies.multi_agent_astar.ThreadedAgent;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedDeque;

public class MultiAgentAStar extends AStrategy {

    public MultiAgentAStar(AHeuristic heuristic) {
        super(heuristic);
    }

    public Solution plan(State initialState) {
        ArrayList<ThreadedAgent> agents = this.getThreadedAgents(this.heuristic, initialState);
        this.startThreads(agents);

        for (ThreadedAgent agent : agents) {
            try {
                agent.join();
            } catch (InterruptedException exc) {
                String errorMessage = "Error! Agent + " + agent.getAgentID() + " + got interrupted.";
                System.err.println(errorMessage);
            }
        }


        Result[] results = new Result[agents.size()];
        for (ThreadedAgent agent : agents) {
            results[agent.getAgentID()] = agent.getResult();
        }

        Action[] actions = null;
        for (Result result : results) {
            if (result.hasPlan()) {
                actions = result.actions;
                break;
            }
        }

        if (actions == null) {
            System.err.println("Error! Multi Agent A* did not find a solution.");
            return null;
        }

        Command[][] plan = this.actions2plan(actions, agents.size());
        PerformanceStats stats = this.getPerformanceStats(results, plan.length);
        return new Solution(plan, stats);
    }

    private ArrayList<ThreadedAgent> getThreadedAgents(AHeuristic heuristic, State initialState) {
        Agent[] agents = initialState.getAgents();
        ArrayList<ThreadedAgent> threadedAgents = new ArrayList<>();
        for (Agent agent : agents) {
            ThreadedAgent threadedAgent = new ThreadedAgent(agent.getId(), heuristic, initialState);
            threadedAgents.add(threadedAgent);
        }
        ArrayList<ConcurrentLinkedDeque<Plan>> messageQueues = new ArrayList<>();
        for (ThreadedAgent threadedAgent : threadedAgents) {
            ConcurrentLinkedDeque<Plan> messageQueue = threadedAgent.getMessageQueue();
            messageQueues.add(messageQueue);
        }
        ConcurrentLinkedDeque<Plan>[] messageQueuesArray = messageQueues.toArray(new ConcurrentLinkedDeque[0]);
        for (ThreadedAgent threadedAgent : threadedAgents) {
            threadedAgent.setOtherAgentsMessageQueues(messageQueuesArray);
        }
        return threadedAgents;
    }

    private void startThreads(ArrayList<ThreadedAgent> agents) {
        for (ThreadedAgent agent : agents) {
            Thread thread = new Thread(agent, agent.toString());
            thread.start();
        }
    }

    private Command[][] actions2plan(Action[] actions, int nAgents) {
        ArrayList<Command[]> jointActions = new ArrayList<>();
        for (Action action : actions) {
            Command[] jointAction = new Command[nAgents];
            for (int i = 0; i < nAgents; i++) {
                jointAction[i] = Command.NoOp;
            }
            jointAction[action.getAgentID()] = action.getCommand();
            jointActions.add(jointAction);
        }
        return jointActions.toArray(new Command[0][0]);
    }

    private PerformanceStats getPerformanceStats(Result[] results, int planLength) {
        long nodesExplored = 0;
        long nodesGenerated = 0;
        long startTime = System.currentTimeMillis();
        double memoryUsed = this.memoryUsed();
        double timeSpent = this.timeSpent(startTime);

        for (Result result : results) {
            nodesExplored += result.nodesExplored;
            nodesGenerated += result.nodesGenerated;
        }

        return new PerformanceStats(memoryUsed, nodesExplored, nodesGenerated, planLength, timeSpent);
    }
}


