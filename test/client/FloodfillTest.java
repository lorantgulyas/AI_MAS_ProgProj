package client;

import client.heuristics.Floodfill;
import client.state.State;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.io.*;

class FloodfillTest {
    static FileReader in;

    @BeforeAll
    static void setUp() throws FileNotFoundException {
        in = new FileReader("src/levels/SARooms.lvl");
    }

    @AfterAll
    static void tearDown() throws IOException {
        in.close();
    }

    @Test
    void readState() throws Exception {
        BufferedReader br = new BufferedReader(in);
        State state = ServerIO.parseState(br);
        // assert here
        System.err.println(state);
        Floodfill ff = new Floodfill(state);
        ff.findRooms(state.getLevel());
        ff.prioritizeGoals(state.getLevel());
    }
}