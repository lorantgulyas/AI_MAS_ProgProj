package subgoaler;

import subgoaler.Floodfill;
import subgoaler.SerializedAStar;

import java.util.ArrayList;

public class Main {
    public static void main(String[] args) throws Exception {
        Client client = new Client("subgoaler");
        State state = client.init();
        //System.err.println(state);

        // preproc
        Floodfill ff = new Floodfill(state);
        ff.findRooms(state);
        ff.prioritizeGoals(state);
        ff.prioritizeBoxes(state);

        // debug goals
//        for (Goal goal : State.getGoals()) {
//            System.err.println(goal);
//        }

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
