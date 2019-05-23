package client.path;

import client.path.comparators.FComparator;
import client.state.Color;
import client.state.Level;
import client.state.Position;
import client.state.State;

/**
 * AStar path that considers all objects as blocks.
 */
public class AllObjectsAStar extends AbstractCachedStateSearch {

    public AllObjectsAStar(int stateSize) {
        super(new FComparator(), stateSize);
    }

    @Override
    protected boolean isFree(Level level, State state, Color color, Position position) {
        return !level.wallAt(position)
                && (!state.boxAt(position) || state.getBoxAt(position).getColor() == color)
                && (!state.agentAt(position) || state.getAgentAt(position).getColor() == color);
    }

}
