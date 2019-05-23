package client.path;

import client.path.comparators.GComparator;
import client.state.Color;
import client.state.Level;
import client.state.Position;
import client.state.State;

public class WallOnlyBFS extends AbstractCachedLevelSearch {

    public WallOnlyBFS(int stateSize) {
        super(new GComparator(), stateSize);
    }

    protected boolean isFree(Level level, State state, Color color, Position position) {
        return !level.wallAt(position);
    }

}
