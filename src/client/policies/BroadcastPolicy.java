package client.policies;

import client.definitions.AMessagePolicy;
import client.state.Agent;
import client.state.State;

import java.util.ArrayList;

/**
 * Policy that dictates to always send a message to all agents.
 */
public class BroadcastPolicy extends AMessagePolicy {

    private ArrayList<ArrayList<Integer>> agents;

    public BroadcastPolicy(State initialState) {
        super(initialState);
        Agent[] agents = initialState.getAgents();
        this.agents = new ArrayList<>(agents.length);
        for (Agent agent : agents) {
            ArrayList<Integer> receivers = new ArrayList<>(agents.length - 1);
            int id = agent.getId();
            for (int i = 0; i < id; i++)
                receivers.add(i);
            for (int i = id + 1; i < agents.length; i++)
                receivers.add(i);
            this.agents.add(receivers);
        }
    }

    @Override
    public Iterable<Integer> receivers(State state, int sender) {
        return this.agents.get(sender);
    }

    @Override
    public String toString() {
        return "broadcast";
    }
}
