package client.definitions;

import client.state.State;

/**
 * Super class of all policies for when to send a message to
 * other agents in Multi-Agent A*.
 */
public abstract class AMessagePolicy {

    public AMessagePolicy(State initialState) {
        // subclasses should do preprocessing here
    }

    /**
     * Finds which agents need to receive messages about the current expanded state.
     *
     * @param state Currently expanded state.
     * @param sender ID of the sending agent.
     * @return
     */
    public abstract Iterable<Integer> receivers(State state, int sender);

}
