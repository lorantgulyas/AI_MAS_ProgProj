package client.definitions;

import client.state.State;

public abstract class AHeuristic {

    public AHeuristic(State initialState) {
      // subclasses should do preprocessing here
    }

    public abstract int h(State state);
}
