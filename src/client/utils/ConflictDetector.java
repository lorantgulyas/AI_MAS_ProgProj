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
            case Move:
                Position moveTo = agentPos.go(command.dir1);
                return !state.isFree(moveTo);
            case Pull:
                Position pullFrom = agentPos.go(command.dir2);
                Position pullTo = agentPos.go(command.dir1);
                return ConflictDetector.boxConflict(state, pullFrom, pullTo, agent);
            case Push:
                Position pushFrom = agentPos.go(command.dir1);
                Position pushTo = pushFrom.go(command.dir2);
                return ConflictDetector.boxConflict(state, pushFrom, pushTo, agent);
            default:
                return false;
        }
    }

    public static boolean conflict(State state, Iterable<Action> jointAction) {
        // check if actions can be applied in the given state
        for (Action action : jointAction) {
            if (ConflictDetector.conflict(state, action)) {
                return true;
            }
        }

        return ConflictDetector.conflict(jointAction);
    }

    public static boolean conflict(Iterable<Action> jointAction) {
        ArrayList<Set<Position>> cellsUsed = new ArrayList<>();
        for (Action action : jointAction) {
            cellsUsed.add(null);
        }
        for (Action action : jointAction) {
            cellsUsed.set(action.getAgentID(), action.getCellsUsed());
        }

        // check that no actions are moving to the same location
        int n = cellsUsed.size();
        for (int i = 0; i < n - 1; i++) {
            Set<Position> cells = cellsUsed.get(i);
            for (Position cell : cells) {
                for (int j = i + 1; j < n; j++) {
                    if (cellsUsed.get(j).contains(cell)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean constrainedConflict(State state, Set<Position> constraints, Action action) {
        Command command = action.getCommand();
        int agentID = action.getAgentID();
        Agent agent = state.getAgents()[agentID];
        Position agentPos = agent.getPosition();
        switch (command.actionType) {
            case Move:
                Position moveTo = agentPos.go(command.dir1);
                return !state.isFree(moveTo);
            case Pull:
                Position pullFrom = agentPos.go(command.dir2);
                Position pullTo = agentPos.go(command.dir1);
                return ConflictDetector.boxConflictConstrained(state, constraints, pullFrom, pullTo, agent);
            case Push:
                Position pushFrom = agentPos.go(command.dir1);
                Position pushTo = pushFrom.go(command.dir2);
                return ConflictDetector.boxConflictConstrained(state, constraints, pushFrom, pushTo, agent);
            default:
                return false;
        }
    }

    public static boolean boxConflict(State currentState, Position from, Position to, Agent agent) {
        return !currentState.boxAt(from)
                || !currentState.isFree(to)
                || currentState.getBoxAt(from).getColor() != agent.getColor();
    }

    public static boolean boxConflictConstrained(State currentState, Set<Position> constraints, Position from, Position to, Agent agent) {
        return !currentState.boxAt(from)
                || !currentState.isFree(to)
                || constraints.contains(from)
                || !constraints.contains(to)
                || currentState.getBoxAt(from).getColor() != agent.getColor();
    }
}
