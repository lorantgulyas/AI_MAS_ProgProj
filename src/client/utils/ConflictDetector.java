package client.utils;

import client.graph.Action;
import client.graph.ActionGenerator;
import client.graph.Command;
import client.state.Agent;
import client.state.Position;
import client.state.State;

import java.util.ArrayList;
import java.util.Set;

public class ConflictDetector {

    public static boolean conflict(State state, Action action) {
        Command command = action.getCommand();
        int agentID = action.getAgentID();
        Agent agent = state.getAgents()[agentID];
        Position agentPos = agent.getPosition();
        switch (command.actionType) {
            case NoOp:
                return false;
            case Move:
                Position moveTo = agentPos.go(command.dir1);
                return !state.isFree(moveTo);
            case Pull:
                Position pullFrom = agentPos.go(command.dir2);
                Position pullTo = agentPos.go(command.dir1);
                return !ActionGenerator.canMoveBox(state, pullFrom, pullTo, agent);
            case Push:
                Position pushFrom = agentPos.go(command.dir1);
                Position pushTo = pushFrom.go(command.dir2);
                return !ActionGenerator.canMoveBox(state, pushFrom, pushTo, agent);
            default:
                return false;
        }
    }

    public static int conflict(State state, Iterable<Action> actions) {
        ArrayList<Set<Position>> cellsUsed = new ArrayList<>();
        int i = 0;
        for (Action action : actions) {
            // check if action can be applied in the given state
            if (ConflictDetector.conflict(state, action)) {
                return i;
            }
            cellsUsed.add(action.getCellsUsed());
            i++;
        }

        // check that no actions are moving to the same location
        int n = cellsUsed.size();
        for (i = 0; i < n; i++) {
            Set<Position> cells = cellsUsed.get(i);
            for (Position cell : cells) {
                for (int j = 0; j < n; j++) {
                    if (i != j) {
                        Set<Position> otherCells = cellsUsed.get(j);
                        if (otherCells.contains(cell)) {
                            return i;
                        }
                    }
                }
            }
        }

        return -1;
    }
}
