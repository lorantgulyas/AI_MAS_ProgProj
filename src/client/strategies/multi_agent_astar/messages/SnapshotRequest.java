package client.strategies.multi_agent_astar.messages;

public class SnapshotRequest<State> extends Message {

    protected String token;
    protected int hash;
    protected State state;

    /**
     * A snapshot request.
     *
     * @param agentID The agent that sent the message.
     * @param token A unique token to identify the snapshot.
     * @param state The state of the sender.
     */
    public SnapshotRequest(int agentID, String token, State state) {
        super(agentID);
        this.token = token;
        this.state = state;
        this.hash = this.computeHashCode();
    }

    public String getToken() {
        return this.token;
    }

    public State getState() {
        return state;
    }

    private int computeHashCode() {
        return (this.agentID + 1) * this.token.hashCode();
    }

    @Override
    public int hashCode() {
        return this.hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null)
            return false;
        if (obj.getClass() != this.getClass())
            return false;
        SnapshotRequest other = (SnapshotRequest) obj;
        return this.token == other.token && this.agentID == other.agentID;
    }

}
