package client.definitions;

import client.state.Position;
import client.state.State;

public abstract class ADistance {

    public abstract int distance(State state, Position p1, Position p2);

}
