package client.strategies.multi_agent_astar;

import client.graph.Action;

public class Result {
    public Action[] actions;
    public long messagesSent;
    public long nodesExplored;
    public long nodesGenerated;

    public Result(Action[] actions, long messagesSent, long nodesExplored, long nodesGenerated) {
        this.actions = actions;
        this.messagesSent = messagesSent;
        this.nodesExplored = nodesExplored;
        this.nodesGenerated = nodesGenerated;
    }

    public boolean hasPlan() {
        return this.actions != null;
    }
}
