package subgoaler;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import java.io.*;
import subgoaler.Floodfill;

public class ClientTest {
    static FileReader in;

    @BeforeAll
    static void setUp() throws FileNotFoundException {
        in = new FileReader("src/levels/comp18/SACybot.lvl");
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
        Box[] prioritizedBoxes = ff.prioritizeBoxes(state.getBoxes());
        state.setBoxes(prioritizedBoxes);
        state = ff.goalDependencies(state);

        Goal[] goals = State.getGoals();
        Box[] boxes = state.getBoxes();
        System.err.println();
        for (int i = 0; i < goals.length; i++) {
            System.err.println(goals[i]);
            System.err.println(boxes[i]);
            System.err.println();
        }
        // temp debug shiet
//        System.err.println("solution: ");
//        for (Task task : tasks) {
//            System.err.println(task.goal);
//            System.err.println(task.box);
//            System.err.println();
//        }

//        SerializedAStar sas = new SerializedAStar(ff);
//        ArrayList<Command> cmds = sas.serializedPlan(state);

//        for (Command cmd : cmds) {
//            System.err.println(cmd);
//        }
    }
}
