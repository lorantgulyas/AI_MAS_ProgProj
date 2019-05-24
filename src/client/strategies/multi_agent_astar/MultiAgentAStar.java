package client.strategies.multi_agent_astar;

import client.PerformanceStats;
import client.Solution;
import client.definitions.AHeuristic;
import client.definitions.AMerger;
import client.definitions.AMessagePolicy;
import client.definitions.AStrategy;
import client.graph.Action;
import client.graph.Command;
import client.state.Agent;
import client.state.Position;
import client.state.State;

import java.util.*;

public class MultiAgentAStar extends AStrategy {

    private AMessagePolicy policy;
    private AMerger merger;
    private int[] agentIDMap;
    private Terminator terminator;

    public MultiAgentAStar(AHeuristic heuristic, AMessagePolicy policy, AMerger merger, int[] agentIDMap) {
        super(heuristic);
        this.terminator = new Terminator();
        this.policy = policy;
        this.merger = merger;
        this.agentIDMap = agentIDMap;
    }

    private int getAgentPrintID(ThreadedAgent agent) {
        return this.agentIDMap[agent.getAgentID()];
    }

    private void killAgents() {
        this.terminator.kill();
    }

    public Solution plan(State initialState) {
        ArrayList<ThreadedAgent> agents = this.getThreadedAgents(this.heuristic, initialState);
        this.startThreads(agents);

        for (ThreadedAgent agent : agents) {
            try {
                agent.join();
            } catch (InterruptedException exc) {
                String errorMessage = "Agent " + this.getAgentPrintID(agent) + " got interrupted.";
                System.err.println(errorMessage);
                this.killAgents();
                return null;
            } catch (OutOfMemoryError exc) {
                String errorMessage = "Maximum memory usage exceeded at agent " + this.getAgentPrintID(agent) + ".";
                System.err.println(errorMessage);
                this.killAgents();
                return null;
            } catch (Exception exc) {
                String errorMessage = "Unknown error at agent " + this.getAgentPrintID(agent) + ": " + exc.getMessage();
                System.err.println(errorMessage);
                this.killAgents();
                return null;
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

        Command[][] plan = actions == null ? new Command[0][0] : this.merger.merge(initialState, actions);
        PerformanceStats stats = this.getPerformanceStats(results);

        if (actions == null) {
            System.err.println("Error! Multi Agent A* did not find a solution.");
        }

        return new Solution(plan, stats);
    }

    private ArrayList<ThreadedAgent> getThreadedAgents(AHeuristic heuristic, State initialState) {
        Agent[] agents = initialState.getAgents();
        ArrayList<ThreadedAgent> threadedAgents = new ArrayList<>();
        for (Agent agent : agents) {
            ThreadedAgent threadedAgent = new ThreadedAgent(
                    this.agentIDMap,
                    agent.getId(),
                    this.terminator,
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


