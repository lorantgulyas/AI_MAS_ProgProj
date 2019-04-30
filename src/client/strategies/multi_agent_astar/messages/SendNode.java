package client.strategies.multi_agent_astar.messages;

import client.graph.Plan;

public class SendNode extends Message {

    private Plan node;

    /**
     * A message containing a node.
     *
     * @param agentID The agent that sent the message.
     * @param node The node from the agent.
     */
    public SendNode(int agentID, Plan node) {
        super(agentID);
        this.node = node;
    }

    public Plan getNode() {
        return this.node;
    }

}
