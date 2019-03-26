package client.definitions;

import client.State;

public abstract class AHeuristic {

    public AHeuristic(AState initialState) {
      // subclasses should do preprocessing here
    }

    public abstract int h(AState state);
}
