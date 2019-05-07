package client.strategies.multi_agent_astar.messages;

public abstract class Message {

    protected int agentID;

    public Message(int agentID) {
        this.agentID = agentID;
    }

    public int getAgentID() {
        return agentID;
    }

}
