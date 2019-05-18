package client;

import client.config.Config;
import client.config.ConfigParser;
import client.definitions.*;

public class ClosedRoomRunner extends Thread{

    private ADistance distance;
    private AHeuristic heuristic;
    private AStrategy strategy;
    private AMessagePolicy messagePolicy;
    private AMerger merger;
    private client.state.State state;
    private Solution solution;

    public ClosedRoomRunner(String unparsedConfig, client.state.State state) throws Exception  {
        Config config = ConfigParser.readConfig(unparsedConfig, state);
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
        int h = this.heuristic.h(this.state);
        this.state.setH(h);
        this.solution = this.strategy.plan(this.state);
    }

}
