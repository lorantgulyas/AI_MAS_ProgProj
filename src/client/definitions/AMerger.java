package client.definitions;

import client.graph.Action;
import client.graph.Command;
import client.state.State;

public abstract class AMerger {

    public abstract Command[][] merge(State initialState, Action[] actions);

}
