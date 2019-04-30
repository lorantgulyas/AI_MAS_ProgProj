package client.strategies.multi_agent_astar;

public class EmptyFrontierSnapshot {

    private int agentID;
    private boolean empty;

    public EmptyFrontierSnapshot(int agentID, boolean empty) {
        this.agentID = agentID;
        this.empty = empty;
    }

    public int getAgentID() {
        return this.agentID;
    }

    public boolean isEmpty() {
        return this.empty;
    }

}
