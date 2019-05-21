package client;

import client.config.Config;
import client.config.ConfigParser;
import client.definitions.*;
import client.graph.Command;
import client.state.SubState;
import subgoaler.Converter;

import java.util.ArrayList;

public class ClosedRoomRunner extends Thread{

    private ADistance distance;
    private AHeuristic heuristic;
    private AStrategy strategy;
    private AMessagePolicy messagePolicy;
    private AMerger merger;
    private client.state.State state;
    private Solution solution;

    /**
     * Class that plans in a thread.
     *
     * @param unparsedConfig Config as loaded from file.
     * @param state The partitioned substate.
     * @param roomSize Number of non-wall cells in the substate.
     * @throws Exception
     */
    public ClosedRoomRunner(String unparsedConfig, SubState state, int roomSize) throws Exception  {
        Config config = ConfigParser.readConfig(unparsedConfig, state.getState(), roomSize, state.getAgentIDMap());
        this.distance = config.getDistance();
        this.heuristic = config.getHeuristic();
        this.strategy = config.getStrategy();
        this.messagePolicy = config.getMessagePolicy();
        this.merger = config.getMerger();
        this.state = state.getState();
    }

    public ADistance getDistance() {
        return this.distance;
    }

    public AHeuristic getHeuristic() {
        return this.heuristic;
    }

    public AStrategy getStrategy() {
        return this.strategy;
    }

    public AMessagePolicy getMessagePolicy() {
        return this.messagePolicy;
    }

    public AMerger getMerger() {
        return this.merger;
    }

    public Solution getSolution() {
        return this.solution;
    }

    public void run() {
        int nAgents = this.state.getAgents().length;
        this.solution = nAgents == 1 ? this.singleAgent() : this.multiAgent();
    }

    private Solution multiAgent() {
        int h = this.heuristic.h(this.state);
        this.state.setH(h);
        return this.strategy.plan(this.state);
    }

    private Solution singleAgent() {
        subgoaler.State SAstate = Converter.convert(state);

        subgoaler.Floodfill ff = new subgoaler.Floodfill(SAstate);
        ff.findRooms(SAstate);
        ff.prioritizeGoals(SAstate);
        ff.prioritizeBoxes(SAstate);

        subgoaler.SerializedAStar sas = new subgoaler.SerializedAStar(ff);
        ArrayList<subgoaler.Command> cmds = sas.serializedPlan(SAstate);
        Command[][] clientCmds = Converter.convertSolution(cmds);

        long explored = sas.nodesExplored();
        long generated = sas.nodesGenerated();
        PerformanceStats stats = new PerformanceStats(0, explored, generated);

        return new Solution(clientCmds, stats);
    }

}
