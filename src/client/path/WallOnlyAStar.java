package client.path;

import client.path.comparators.FComparator;
import client.state.Color;
import client.state.Level;
import client.state.Position;
import client.state.State;

/**
 * AStar that only considers walls as blocks.
 */
public class WallOnlyAStar extends AbstractCachedLevelSearch {

    public WallOnlyAStar(int stateSize) {
        super(new FComparator(), stateSize);
    }

    protected boolean isFree(Level level, State state, Color color, Position position) {
        return !level.wallAt(position);
    }

}
