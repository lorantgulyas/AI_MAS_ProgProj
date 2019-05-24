package client;

import client.config.Config;
import client.config.ConfigParser;
import client.definitions.*;
import client.state.SubState;

import java.util.concurrent.Callable;

public class ConfigRunner implements Callable<Solution> {

    private ADistance distance;
    private AHeuristic heuristic;
    private AStrategy strategy;
    private AMessagePolicy messagePolicy;
    private AMerger merger;
    private client.state.State state;

    /**
     * Class that plans in a thread.
     *
     * @param configPath  Path to configuration.
     * @param state The partitioned substate.
     * @param roomSize Number of non-wall cells in the substate.
     * @throws Exception
     */
    ConfigRunner(String configPath, SubState state, int roomSize) throws Exception  {
        Config config = ConfigParser.readConfigFromFile(configPath, state.getState(), roomSize, state.getAgentIDMap());
        this.distance = config.getDistance();
        this.heuristic = config.getHeuristic();
        this.strategy = config.getStrategy();
        this.messagePolicy = config.getMessagePolicy();
        this.merger = config.getMerger();
        this.state = state.getState();
    }

    @Override
    public Solution call() throws Exception {
        int h = this.heuristic.h(this.state);
        this.state.setH(h);
        return this.strategy.plan(this.state);
    }

}
