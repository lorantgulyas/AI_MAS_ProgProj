package client.strategies.multi_agent_astar.messages;

public class EmptyFrontierRequest extends SnapshotRequest<Boolean> {

    /**
     * A empty frontier snapshot request.
     *
     * @param agentID The agent that sent the message.
     * @param token A unique token to identify the snapshot.
     * @param state The state of the sender.
     */
    public EmptyFrontierRequest(int agentID, String token, Boolean state) {
        super(agentID, token, state);
    }

}
