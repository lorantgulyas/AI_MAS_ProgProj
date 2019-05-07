package client.strategies;

import client.PerformanceStats;
import client.Solution;
import client.definitions.AHeuristic;
import client.definitions.AStrategy;
import client.graph.Action;
import client.graph.Command;
import client.graph.Plan;
import client.state.Agent;
import client.state.Position;
import client.state.State;
import client.strategies.multi_agent_astar.Result;
import client.strategies.multi_agent_astar.ThreadedAgent;

import java.util.*;

public class MultiAgentAStar extends AStrategy {

    public MultiAgentAStar(AHeuristic heuristic) {
        super(heuristic);
    }

    public Solution plan(State initialState) {
        long startTime = System.currentTimeMillis();
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

        Command[][] plan = actions == null ? new Command[0][0] : this.actions2plan(actions, agents.size());
        PerformanceStats stats = this.getPerformanceStats(results, plan.length, startTime);

        if (actions == null) {
            System.err.println("Error! Multi Agent A* did not find a solution.");
        }

        return new Solution(plan, stats);
    }

    private ArrayList<ThreadedAgent> getThreadedAgents(AHeuristic heuristic, State initialState) {
        Agent[] agents = initialState.getAgents();
        ArrayList<ThreadedAgent> threadedAgents = new ArrayList<>();
        for (Agent agent : agents) {
            ThreadedAgent threadedAgent = new ThreadedAgent(agent.getId(), heuristic, initialState);
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

    private Command[][] actions2plan(Action[] actions, int nAgents) {
        /*ArrayList<Command[]> jointActions = new ArrayList<>();
        for (Action action : actions) {
            Command[] jointAction = new Command[nAgents];
            for (int i = 0; i < nAgents; i++) {
                jointAction[i] = Command.NoOp;
            }
            jointAction[action.getAgentID()] = action.getCommand();
            jointActions.add(jointAction);
        }*/

        final long startTime;
        startTime = System.currentTimeMillis();

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

        boolean done = false;
        while (!done) {

            Boolean[] agentActionCanBeMerged = new Boolean[nAgents];
            Arrays.fill(agentActionCanBeMerged, Boolean.TRUE);

            for (int i = 0; i < container.length; i++) {
                if (!container[i].isEmpty() && i != container.length - 1) {
                    thisAgent:
                    for (Position position : ((Action) container[i].getFirst()).getCellsUsed()) {
                        for (int j = 1; j < nAgents; j++) {
                            if (i + j < container.length) {
                                for (int k = 0; k < container[i + j].size(); k++) {
                                    Action otherAgentAction = (Action) container[i + j].get(k);
                                    if (otherAgentAction.getCellsUsed().contains(position)) {
                                        //The two actions from agent i and agent i+j interferes on cellsUsed
                                        //Therefore for now, agent i cannot be used
                                        agentActionCanBeMerged[i] = false;
                                        break thisAgent;
                                    } else if (otherAgentAction.getCellsUsed().contains(new Position(position.getCol() - 1, position.getRow()))) {
                                        agentActionCanBeMerged[i] = false;
                                        break thisAgent;
                                    } else if (otherAgentAction.getCellsUsed().contains(new Position(position.getCol() + 1, position.getRow()))) {
                                        agentActionCanBeMerged[i] = false;
                                        break thisAgent;
                                    } else if (otherAgentAction.getCellsUsed().contains(new Position(position.getCol(), position.getRow() - 1))) {
                                        agentActionCanBeMerged[i] = false;
                                        break thisAgent;
                                    } else if (otherAgentAction.getCellsUsed().contains(new Position(position.getCol(), position.getRow() + 1))) {
                                        agentActionCanBeMerged[i] = false;
                                        break thisAgent;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Command[] combinedAction = new Command[nAgents];

            for (int i = 0; i < nAgents; i++) {
                if (agentActionCanBeMerged[i] && container[i].size() != 0) {
                    Action agentAction = (Action) container[i].poll();
                    combinedAction[i] = agentAction.getCommand();
                } else {
                    combinedAction[i] = Command.NoOp;
                }
            }

            combinedActions.add(combinedAction);

            boolean allActionsAreMerged = true;
            for (int i = 0; i < container.length; i++) {
                if (!container[i].isEmpty()) {
                    allActionsAreMerged = false;
                    break;
                }
            }

            done = allActionsAreMerged;
        }
        System.err.println(System.currentTimeMillis() - startTime / 1000f);
        return combinedActions.toArray(new Command[0][0]);
    }

    private PerformanceStats getPerformanceStats(Result[] results, int planLength, long startTime) {
        long nodesExplored = 0;
        long nodesGenerated = 0;
        double memoryUsed = this.memoryUsed();
        double timeSpent = this.timeSpent(startTime);

        for (Result result : results) {
            nodesExplored += result.nodesExplored;
            nodesGenerated += result.nodesGenerated;
        }

        return new PerformanceStats(memoryUsed, nodesExplored, nodesGenerated, planLength, timeSpent);
    }
}


