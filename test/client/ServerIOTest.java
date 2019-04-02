package client;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

class ServerIOTest {
    static FileReader in;

    @BeforeAll
    static void setUp() throws FileNotFoundException {
        in = new FileReader("./MAExample.lvl");
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
    }
}