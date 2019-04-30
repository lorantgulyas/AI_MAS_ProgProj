package client.strategies.multi_agent_astar;

public class SolutionSnapshot {

    private int agentID;
    private int lowerBound;

    public SolutionSnapshot(int agentID, int lowerBound) {
        this.agentID = agentID;
        this.lowerBound = lowerBound;
    }

    public int getAgentID() {
        return this.agentID;
    }

    public int getLowerBound() {
        return this.lowerBound;
    }

}
