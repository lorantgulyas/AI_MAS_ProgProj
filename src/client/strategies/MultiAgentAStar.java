package client.strategies;

import client.PerformanceStats;
import client.Solution;
import client.definitions.AHeuristic;
import client.definitions.AMessagePolicy;
import client.definitions.AStrategy;
import client.graph.Action;
import client.graph.Command;
import client.graph.StateGenerator;
import client.state.Agent;
import client.state.State;
import client.strategies.multi_agent_astar.Result;
import client.strategies.multi_agent_astar.Terminator;
import client.strategies.multi_agent_astar.ThreadedAgent;
import client.utils.ConflictDetector;

import java.util.ArrayList;
import java.util.LinkedList;

public class MultiAgentAStar extends AStrategy {

    private AMessagePolicy policy;

    public MultiAgentAStar(AHeuristic heuristic, AMessagePolicy policy) {
        super(heuristic);
        this.policy = policy;
    }

    public Solution plan(State initialState) {
        long startTime = System.currentTimeMillis();
        ArrayList<ThreadedAgent> agents = this.getThreadedAgents(this.heuristic, initialState);
        this.startThreads(agents);

        for (ThreadedAgent agent : agents) {
            try {
                agent.join();
            } catch (InterruptedException exc) {
                String errorMessage = "Agent + " + agent.getAgentID() + " + got interrupted.";
                System.err.println(errorMessage);
            } catch (OutOfMemoryError exc) {
                String errorMessage = "Maximum memory usage exceeded at agent " + agent.getAgentID() + ".";
                System.err.println(errorMessage);
            } catch (Exception exc) {
                String errorMessage = "Unknown error at agent " + agent.getAgentID() + ": " + exc.getMessage();
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

        Command[][] plan = actions == null ? new Command[0][0] : this.actions2plan(initialState, actions, agents.size());
        PerformanceStats stats = this.getPerformanceStats(results, plan.length, startTime);

        if (actions == null) {
            System.err.println("Error! Multi Agent A* did not find a solution.");
        }

        return new Solution(plan, stats);
    }

    private ArrayList<ThreadedAgent> getThreadedAgents(AHeuristic heuristic, State initialState) {
        Agent[] agents = initialState.getAgents();
        Terminator terminator = new Terminator();
        ArrayList<ThreadedAgent> threadedAgents = new ArrayList<>();
        for (Agent agent : agents) {
            ThreadedAgent threadedAgent = new ThreadedAgent(
                    agent.getId(),
                    terminator,
                    heuristic,
                    this.policy,
                    initialState
            );
            // make absolutely sure that all agents run with the same priority
            // for some reason this seems to be necessary
            threadedAgent.setPriority(5);
            threadedAgents.add(threadedAgent);
        }
        ThreadedAgent[] threadedAgentsArray = threadedAgents.toArray(new ThreadedAgent[0]);
        for (ThreadedAgent threadedAgent : threadedAgents) {
            threadedAgent.setOtherAgents(threadedAgentsArray);
        }
        return threadedAgents;
    }

    private void startThreads(ArrayList<ThreadedAgent> agents) {
        for (ThreadedAgent agent : agents) {
            agent.start();
        }
    }

    private Command[][] actions2plan(State initialState, Action[] actions, int nAgents) {

        ArrayList<Command[]> combinedActions = new ArrayList<>();
        LinkedList[] container = new LinkedList[nAgents];

        for (int i = 0; i < nAgents; i++) {
            LinkedList<Action> myActions = new LinkedList<>();
            for (Action action : actions) {
                if (action.getAgentID() == i) {
                    myActions.add(action);
                }
            }
            container[i] = myActions;
        }

        State state = initialState;

        boolean done = false;
        while (!done) {

            ArrayList<Action> topActions = new ArrayList<>();
            for (int i = 0; i < container.length; i++) {
                Action agentAction = (Action) container[i].poll();
                topActions.add(agentAction);
            }

            for (int i = nAgents; 0 < i; i--) {
                ArrayList<Action> remainingActions = new ArrayList<>();
                for (Action action : actions) {
                    if (!topActions.contains(action)) {
                        remainingActions.add(action);
                    }
                }


            }


            boolean merged = detectConflict(state, topActions, remainingActions);

            boolean allActionsAreMerged = true;
            for (int i = 0; i < container.length; i++) {
                if (!container[i].isEmpty()) {
                    allActionsAreMerged = false;
                    break;
                }
            }
            done = allActionsAreMerged;

        }

        return combinedActions.toArray(new Command[0][0]);
    }

    private boolean detectConflict(State state, ArrayList<Action> topActions, Iterable<Action> remainingActions) {
        if (ConflictDetector.conflict(state, topActions) != -1) {
            return true;
        }

        state = StateGenerator.generate(state, topActions);

        for (Action action : remainingActions) {
            if (ConflictDetector.conflict(state, action)) {
                return true;
            }
            state = StateGenerator.generate(state, action);
        }
        return false;
    }

    private PerformanceStats getPerformanceStats(Result[] results, int planLength, long startTime) {
        long messagesSent = 0;
        long nodesExplored = 0;
        long nodesGenerated = 0;
        double memoryUsed = this.memoryUsed();
        double timeSpent = this.timeSpent(startTime);

        for (Result result : results) {
            messagesSent += result.messagesSent;
            nodesExplored += result.nodesExplored;
            nodesGenerated += result.nodesGenerated;
        }

        return new PerformanceStats(memoryUsed, messagesSent, nodesExplored, nodesGenerated, planLength, timeSpent);
    }

    @Override
    public String toString() {
        return "Multi-Agent A*";
    }
}


