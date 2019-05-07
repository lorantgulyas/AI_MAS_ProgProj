package client.strategies.multi_agent_astar.messages;

public class EmptyFrontierResponse extends SnapshotResponse<EmptyFrontierRequest, Boolean> {

    /**
     * A snapshot response.
     *
     * @param request The snapshot request that this is a response to.
     * @param agentID ID of the agent that is sending the response.
     * @param state State of the sending agent.
     */
    public EmptyFrontierResponse(EmptyFrontierRequest request, int agentID,  boolean state) {
        super(request, agentID, state);
    }

}
