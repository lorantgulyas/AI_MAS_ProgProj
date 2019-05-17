package client.strategies.multi_agent_astar;

import client.PerformanceStats;
import client.Solution;
import client.definitions.AHeuristic;
import client.definitions.AMessagePolicy;
import client.definitions.AStrategy;
import client.graph.Action;
import client.graph.Command;
import client.state.Agent;
import client.state.State;

import java.util.ArrayList;

public class MultiAgentAStar extends AStrategy {

    private AMessagePolicy policy;

    public MultiAgentAStar(AHeuristic heuristic, AMessagePolicy policy) {
        super(heuristic);
        this.policy = policy;
    }

    public Solution plan(State initialState) {
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

        Command[][] plan = actions == null ? new Command[0][0] : this.actions2plan(initialState, actions);
        PerformanceStats stats = this.getPerformanceStats(results);

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

    private Command[][] actions2plan(State initialState, Action[] actions) {
        int nAgents = initialState.getAgents().length;
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
        /*
        ArrayList<ArrayList<Action>> jointActions = ActionMerger.merge(initialState, actions);
        Command[][] plan = new Command[jointActions.size()][];
        int i = 0;
        for (ArrayList<Action> jointAction : jointActions) {
            Command[] commands = new Command[jointAction.size()];
            int j = 0;
            for (Action action : jointAction) {
                commands[j] = action.getCommand();
                j++;
            }
            plan[i] = commands;
            i++;
        }
        return plan;
        */
    }



    private PerformanceStats getPerformanceStats(Result[] results) {
        long messagesSent = 0;
        long nodesExplored = 0;
        long nodesGenerated = 0;

        for (Result result : results) {
            messagesSent += result.messagesSent;
            nodesExplored += result.nodesExplored;
            nodesGenerated += result.nodesGenerated;
        }

        return new PerformanceStats(messagesSent, nodesExplored, nodesGenerated);
    }

    @Override
    public String toString() {
        return "Multi-Agent A*";
    }
}


