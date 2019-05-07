package client.strategies.multi_agent_astar.messages;

public class SnapshotResponse<Request, ResponseState> extends Message {

    protected int hash;
    protected Request request;
    protected ResponseState state;

    /**
     * A snapshot response.
     *
     * @param request The snapshot request that this is a response to.
     * @param agentID ID of the agent that is sending the response.
     * @param state State of the sending agent.
     */
    public SnapshotResponse(Request request, int agentID,  ResponseState state) {
        super(agentID);
        this.request = request;
        this.state = state;
        this.hash = this.computeHashCode();
    }

    public Request getRequest() {
        return request;
    }

    public ResponseState getState() {
        return state;
    }

    private int computeHashCode() {
        return (this.agentID + 1) * this.request.hashCode();
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
        SnapshotResponse other = (SnapshotResponse) obj;
        return this.agentID == other.agentID && this.request.equals(other.request);
    }

}
