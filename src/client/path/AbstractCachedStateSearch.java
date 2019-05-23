package client.path;

import client.state.Color;
import client.state.Position;
import client.state.State;

import java.util.Comparator;
import java.util.HashMap;

abstract class AbstractCachedStateSearch extends AbstractSearch {

    private HashMap<StateInput, Node> results;

    AbstractCachedStateSearch(Comparator<Node> comparator, int stateSize) {
        super(comparator, stateSize);
        this.results = new HashMap<>();
    }

    @Override
    protected Node search(State state, Position p1, Position p2, Color color, int maxDistance) {
        StateInput input = new StateInput(state, p1, p2, color, maxDistance);
        if (this.results.containsKey(input))
            return this.results.get(input);

        Node result = super.search(state, p1, p2, color, maxDistance);
        this.insertResult(input, result);
        return result;
    }

    private synchronized void insertResult(StateInput input, Node result) {
        results.put(input, result);
    }

}
