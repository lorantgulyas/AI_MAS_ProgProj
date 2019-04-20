package client;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.io.*;

class ServerIOTest {
    static FileReader in;

    @BeforeAll
    static void setUp() throws FileNotFoundException {
        in = new FileReader("src/levels/MAExample.lvl");
    }

    @AfterAll
    static void tearDown() throws IOException {
        in.close();
    }

    @Test
    void readState() {
        Assertions.assertDoesNotThrow(new Executable() {
            @Override
            public void execute() throws Throwable {
                BufferedReader br = new BufferedReader(in);
                ServerIO.parseState(br);
            }
        });
    }
}