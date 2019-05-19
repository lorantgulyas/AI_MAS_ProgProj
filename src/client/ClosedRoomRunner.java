package client;

import client.config.Config;
import client.config.ConfigParser;
import client.definitions.*;
import client.graph.Command;
import client.state.Box;
import client.strategies.serialized_astar.Floodfill;
import client.strategies.serialized_astar.SerializedAStar;

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
    public ClosedRoomRunner(String unparsedConfig, client.state.State state, int roomSize) throws Exception  {
        Config config = ConfigParser.readConfig(unparsedConfig, state, roomSize);
        this.distance = config.getDistance();
        this.heuristic = config.getHeuristic();
        this.strategy = config.getStrategy();
        this.messagePolicy = config.getMessagePolicy();
        this.merger = config.getMerger();
        this.state = state;
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
        Floodfill ff = new Floodfill(state);
        ff.findRooms();
        ff.prioritizeGoals();
        Box[] prioritizedBoxes = ff.prioritizeBoxes(this.state.getBoxes());
        this.state.setBoxes(prioritizedBoxes);
        this.state = ff.goalDependencies(state);
        prioritizedBoxes = ff.prioritizeBoxes(state.getBoxes());
        this.state.setBoxes(prioritizedBoxes);
        SerializedAStar sas = new SerializedAStar(ff);
        ArrayList<Command> cmds = sas.serializedPlan(state);

        long explored = sas.nodesExplored();
        long generated = sas.nodesGenerated();
        PerformanceStats stats = new PerformanceStats(0, explored, generated);

        Command[][] plan = new Command[cmds.size()][1];
        for (int i = 0; i < cmds.size(); i++) {
            plan[i][0] = cmds.get(i);
        }

        return new Solution(plan, stats);
    }

}
