package client.strategies.multi_agent_astar;

import client.graph.Plan;
import client.strategies.multi_agent_astar.messages.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public class Channel {

    private int agentID;
    private long clock;
    private Channel[] channels;
    private HashMap<Integer, Channel> channelsMap;
    private ConcurrentLinkedDeque<Message> queue;
    private long messagesSent;

    public Channel(int agentID) {
        this.agentID = agentID;
        this.clock = 0;
        this.queue = new ConcurrentLinkedDeque<>();
        this.messagesSent = 0;
    }

    private Channel[] findOtherChannels(ThreadedAgent[] agents) {
        Channel[] otherAgents = new Channel[agents.length - 1];
        int j = 0;
        for (int i = 0; i < agents.length; i++) {
            if (i != this.agentID) {
                otherAgents[j] = agents[i].getChannel();
                j++;
            }
        }
        return otherAgents;
    }

    public int getAgentID() {
        return agentID;
    }

    public long getMessagesSent() {
        return this.messagesSent;
    }

    /**
     * Delivers the earliest received message.
     * This will remove the message from the queue.
     *
     * @return The earliest received message.
     */
    public Message deliver() {
        return this.queue.poll();
    }

    /**
     * Delivers all the received messages.
     * The messages will be removed from the queue.
     *
     * @return The received messages in order of when the were received.
     */
    public ArrayList<Message> deliverAll() {
        ArrayList<Message> messages = new ArrayList<>();
        while (!this.queue.isEmpty()) {
            Message message = this.queue.poll();
            messages.add(message);
        }
        return messages;
    }

    public boolean isEmpty() {
        return this.queue.isEmpty();
    }

    /**
     * Assumes that the agents also contain the agent itself.
     * Must be called before the channel can be used.
     *
     * @param agents
     */
    public void setChannels(ThreadedAgent[] agents) {
        this.channels = this.findOtherChannels(agents);
        this.channelsMap = new HashMap<>();
        for (Channel agent : this.channels) {
            this.channelsMap.put(agent.getAgentID(), agent);
        }
    }

    /**
     * Receives a message from another channel and adds it to the tail of the queue.
     *
     * @param message The message sent from another agent.
     */
    public void receive(Message message) {
        this.queue.add(message);
        this.messagesSent++;
    }

    /**
     * Sends a node to all other agents.
     *
     * @param node The node to broadcast.
     */
    public void broadcast(Plan node) {
        SendNode message = new SendNode(this.agentID, node);
        for (Channel channel : this.channels) {
            channel.receive(message);
        }
        this.messagesSent += this.channels.length;
    }

    /**
     * Sends a node to an agent.
     *
     * @param toAgentID The agent to send the message to.
     * @param node The node to send.
     */
    public void sendTo(int toAgentID, Plan node) {
        Channel channel = this.channelsMap.get(toAgentID);
        SendNode message = new SendNode(this.agentID, node);
        channel.receive(message);
        this.messagesSent++;
    }

    /**
     * Sends a node to multiple agents.
     *
     * @param toAgentIDs Agents to send the message to.
     * @param node The node to send.
     */
    public void sendTo(Iterable<Integer> toAgentIDs, Plan node) {
        SendNode message = new SendNode(this.agentID, node);
        for (int toAgentID : toAgentIDs) {
            Channel channel = this.channelsMap.get(toAgentID);
            channel.receive(message);
            this.messagesSent++;
        }
    }

    /**
     * Creates a request to verify that their frontiers are empty.
     *
     * @param empty The state of frontier of the sending agent.
     * @return The snapshot request message.
     */
    public EmptyFrontierRequest makeEmptyFrontierRequest(boolean empty) {
        String token = "empty-frontier:" + this.agentID + ":" + this.clock;
        this.clock++;
        return new EmptyFrontierRequest(this.agentID, token, empty);
    }

    /**
     * Sends a message to all other agents to verify that their frontiers are empty.
     */
    public void sendEmptyFrontierRequest(EmptyFrontierRequest request) {
        for (Channel channel : this.channels) {
            channel.receive(request);
        }
        this.messagesSent += this.channels.length;
    }

    /**
     * Sends a response to an empty frontier snapshot request.
     *
     * @param request Request from the initiating process.
     * @param empty State of the frontier of the agent.
     */
    public void sendEmptyFrontierResponse(EmptyFrontierRequest request, boolean empty) {
        EmptyFrontierResponse response = new EmptyFrontierResponse(request, this.agentID, empty);
        Channel channel = this.channelsMap.get(request.getAgentID());
        channel.receive(response);
        this.messagesSent++;
    }
}
