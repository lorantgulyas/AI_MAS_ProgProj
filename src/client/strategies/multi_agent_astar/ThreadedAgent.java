package client.strategies.multi_agent_astar;

import client.graph.Action;
import client.graph.Plan;

import java.util.concurrent.ConcurrentLinkedDeque;

public class ThreadedAgent extends Thread {

    private int agentID;
    private ConcurrentLinkedDeque<Plan> messageQueue;
    private ConcurrentLinkedDeque<Plan>[] otherAgentsMessageQueues;

    public ThreadedAgent(int agentID) {
        this.agentID = agentID;
        this.messageQueue = new ConcurrentLinkedDeque<>();
    }

    public int getAgentID() {
        return agentID;
    }

    public ConcurrentLinkedDeque<Plan> getMessageQueue() {
        return messageQueue;
    }

    public void setOtherAgentsMessageQueues(ConcurrentLinkedDeque<Plan>[] otherAgentsMessageQueues) {
        this.otherAgentsMessageQueues = otherAgentsMessageQueues;
    }

    public Result getResult() {
        // TODO
        return null;
    }

    public void run() {
        // TODO
    }

    @Override
    public String toString() {
        return "Agent " + this.agentID;
    }
}
