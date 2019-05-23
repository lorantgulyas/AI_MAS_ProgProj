package client;

import client.distance.LazyShortestPath;
import client.heuristics.SingleTasker;
import client.path.AllObjectsAStar;
import client.path.WallOnlyAStar;
import client.state.State;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SingleTaskerTest {

    private State multiAgent;
    private State solved;
    private State singleAgent;

    public SingleTaskerTest() {
        try {
            this.multiAgent = ServerIO.readFromFile("test/levels/MABoxBlockedPath.lvl");
            this.solved = ServerIO.readFromFile("test/levels/MASolved.lvl");
            this.singleAgent = ServerIO.readFromFile("test/levels/SA3x3PullWest.lvl");
        } catch (Exception exc) {
            // do nothing
        }
    }

    @Test
    public void worksForSingleAgent() {
        // V = 5
        // # goals = 1
        // # boxes = 1
        // box to goal distance = 2
        // agent to goal-box distance = 1
        int stateSize = 5;
        int unmatchedGoals = 1;
        WallOnlyAStar wallPlanner = new WallOnlyAStar(stateSize);
        LazyShortestPath measurer = new LazyShortestPath(this.singleAgent, stateSize, wallPlanner);
        SingleTasker heuristic = new SingleTasker(this.singleAgent, measurer, stateSize);
        int expected = 2 + 1 + unmatchedGoals * stateSize;
        int actual = heuristic.h(this.singleAgent);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void worksForMultiAgent() {
        // V = 16
        // # goals = 2
        // # boxes = 2
        // box A to goal distance = 6
        // box B to goal distance = 1
        // agent 0 to goal-box distance = 1
        // agent 1 to goal-box distance = 7
        int stateSize = 16;
        int unmatchedGoals = 1;
        WallOnlyAStar wallPlanner = new WallOnlyAStar(stateSize);
        LazyShortestPath measurer = new LazyShortestPath(this.multiAgent, stateSize, wallPlanner);
        SingleTasker heuristic = new SingleTasker(this.multiAgent, measurer, stateSize);
        int expected = (6 + 1 + unmatchedGoals * stateSize) + (1 + 7 + unmatchedGoals * stateSize);
        int actual = heuristic.h(this.multiAgent);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void computesZeroForASolvedLevel() {
        int stateSize = 25;
        WallOnlyAStar wallPlanner = new WallOnlyAStar(stateSize);
        LazyShortestPath measurer = new LazyShortestPath(this.solved, stateSize, wallPlanner);
        SingleTasker heuristic = new SingleTasker(this.solved, measurer, stateSize);
        int expected = 0;
        int actual = heuristic.h(this.solved);
        Assertions.assertEquals(expected, actual);
    }

}
