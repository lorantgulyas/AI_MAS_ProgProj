package client.heuristics.unblocker;

import client.definitions.ADistance;
import client.state.State;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class BlockedFinder {

    private int agentID;
    private ADistance measurer;

    public BlockedFinder(int agentID, State initialState, ADistance measurer) {
        this.agentID = agentID;
        this.measurer = measurer;
    }

    public ArrayList<Block> getBlocks(State state) {
        ArrayList<Block> blocks = new ArrayList<>();
        return blocks;
    }

}
