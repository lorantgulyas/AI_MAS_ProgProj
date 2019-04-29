package subgoaler;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import java.io.*;
import java.util.ArrayList;

public class ClientTest {
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
        State state = Client.parseState(br);
        System.err.println(state);
        Floodfill ff = new Floodfill(state);
        ff.findRooms();
        ff.prioritizeGoals();

        SerializedAStar sas = new SerializedAStar(ff);
        ArrayList<Command> cmds = sas.serializedPlan(state);

//        for (Command cmd : cmds) {
//            System.err.println(cmd);
//        }
    }
}
