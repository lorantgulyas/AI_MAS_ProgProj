package client.definitions;

import client.graph.Plan;
import client.state.State;

import java.util.Comparator;

public abstract class AHeuristic implements Comparator<Plan> {

    public AHeuristic(State initialState) {
      // subclasses should do preprocessing here
    }

    @Override
    public int compare(Plan p1, Plan p2) {
        return this.f(p1) - this.f(p2);
    }

    public int f(Plan p) {
        return p.g() + this.h(p.getState());
    }

    public abstract int h(State state);
}
