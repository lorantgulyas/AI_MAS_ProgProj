package client.strategies.multi_agent_astar;

import client.graph.Plan;
import client.strategies.multi_agent_astar.messages.Message;
import client.strategies.multi_agent_astar.messages.SendNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public class Channel {

    private int agentID;
    private long clock;
    private Channel[] channels;
    private HashMap<Integer, Channel> channelsMap;
    private ConcurrentLinkedDeque<Message> queue;

    public Channel(int agentID) {
        this.agentID = agentID;
        this.clock = 0;
        this.queue = new ConcurrentLinkedDeque<>();
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

    public Message deliver() {
        return this.queue.poll();
    }

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

    public void receive(Message message) {
        this.queue.add(message);
    }

    public void sendTo(int toAgentID, Plan node) {
        Channel channel = this.channelsMap.get(toAgentID);
        SendNode message = new SendNode(this.agentID, node);
        channel.receive(message);
    }

    public void broadcast(Plan node) {
        SendNode message = new SendNode(this.agentID, node);
        for (Channel channel : this.channels) {
            channel.receive(message);
        }
    }

}
