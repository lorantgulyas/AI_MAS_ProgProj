package client.strategies.multi_agent_astar;

import client.graph.Action;

public class Result {
    public Action[] actions;
    public long nodesExplored;
    public long nodesGenerated;

    public Result(Action[] actions, long nodesExplored, long nodesGenerated) {
        this.actions = actions;
        this.nodesExplored = nodesExplored;
        this.nodesGenerated = nodesGenerated;
    }

    public boolean hasPlan() {
        // TODO
        return false;
    }
}
