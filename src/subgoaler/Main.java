package subgoaler;

import java.util.ArrayList;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) throws Exception {
        Client client = new Client("subgoaler");
        State state = client.init();
        System.err.println(state);

        Floodfill ff = new Floodfill(state);
        ff.findRooms();
        ff.prioritizeGoals();
        Box[] prioritizedBoxes = ff.prioritizeBoxes(state.getBoxes());
        state.setBoxes(prioritizedBoxes);
        state = ff.goalDependencies(state);
        prioritizedBoxes = ff.prioritizeBoxes(state.getBoxes());
        state.setBoxes(prioritizedBoxes);

        // debug goals
        for (Goal goal : State.getGoals()) {
            System.err.println(goal);
        }

        SerializedAStar sas = new SerializedAStar(ff);
        ArrayList<Command> cmds = sas.serializedPlan(state);

        for (Command cmd : cmds) {
//            System.err.println(cmd);
            Command[] kek = new Command[1];
            kek[0] = cmd;
            client.sendCommands(kek);
        }
    }
}
