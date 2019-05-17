package client.mergers;

import client.definitions.AMerger;
import client.graph.Action;
import client.graph.Command;
import client.state.State;

public class NoMerge extends AMerger {

    public Command[][] merge(State initialState, Action[] actions) {
        int nAgents = initialState.getAgents().length;
        int nActions = actions.length;
        Command[][] plan = new Command[nActions][nAgents];
        for (int i = 0; i < nActions; i++) {
            Action action = actions[i];
            int id = action.getAgentID();
            for (int j = 0; j < id; j++) {
                plan[i][j] = Command.NoOp;
            }
            plan[i][id] = action.getCommand();
            for (int j = id + 1; j < nAgents; j++) {
                plan[i][j] = Command.NoOp;
            }
        }
        return plan;
    }

    @Override
    public String toString() {
        return "no-merge";
    }

}
