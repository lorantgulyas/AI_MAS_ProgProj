package client;

import client.heuristics.SingleTaskerShortestPath;
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
        int expected = 2 + 1 + 1 * 5;
        int actual = new SingleTaskerShortestPath(this.singleAgent).h(this.singleAgent);
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
        int expected = (6 + 1 + 1 * 16) + (1 + 7 + 1 * 16);
        int actual = new SingleTaskerShortestPath(this.multiAgent).h(this.multiAgent);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void computesZeroForASolvedLevel() {
        int expected = 0;
        int actual = new SingleTaskerShortestPath(this.solved).h(this.solved);
        Assertions.assertEquals(expected, actual);
    }

}
