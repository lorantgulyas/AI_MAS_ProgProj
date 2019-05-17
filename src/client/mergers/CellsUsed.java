package client.mergers;

import client.definitions.AMerger;
import client.graph.Action;
import client.graph.Command;
import client.state.Position;
import client.state.State;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

public class CellsUsed extends AMerger {

    public Command[][] merge(State initialState, Action[] actions) {
        int nAgents = initialState.getAgents().length;
        ArrayList<Command[]> combinedActions = new ArrayList<>();
        LinkedList[] container = new LinkedList[nAgents];

        for (int i = 0; i < nAgents; i++) {
            LinkedList<Action> myActions = new LinkedList<>();
            for (Action action : actions) {
                if (action.getAgentID() == i) {
                    myActions.add(action);
                }
            }
            container[i] = myActions;
        }

        boolean done = false;
        while (!done) {

            Boolean[] agentActionCanBeMerged = new Boolean[nAgents];
            Arrays.fill(agentActionCanBeMerged, Boolean.TRUE);

            for (int i = 0; i < container.length; i++) {
                if (!container[i].isEmpty() && i != container.length - 1) {
                    thisAgent:
                    for (Position position : ((Action) container[i].getFirst()).getCellsUsed()) {
                        for (int j = 1; j < nAgents; j++) {
                            if (i + j < container.length) {
                                for (int k = 0; k < container[i + j].size(); k++) {
                                    Action otherAgentAction = (Action) container[i + j].get(k);
                                    if (otherAgentAction.getCellsUsed().contains(position)) {
                                        //The two actions from agent i and agent i+j interferes on cellsUsed
                                        //Therefore for now, agent i cannot be used
                                        agentActionCanBeMerged[i] = false;
                                        break thisAgent;
                                    } else if (otherAgentAction.getCellsUsed().contains(new Position(position.getCol() - 1, position.getRow()))) {
                                        agentActionCanBeMerged[i] = false;
                                        break thisAgent;
                                    } else if (otherAgentAction.getCellsUsed().contains(new Position(position.getCol() + 1, position.getRow()))) {
                                        agentActionCanBeMerged[i] = false;
                                        break thisAgent;
                                    } else if (otherAgentAction.getCellsUsed().contains(new Position(position.getCol(), position.getRow() - 1))) {
                                        agentActionCanBeMerged[i] = false;
                                        break thisAgent;
                                    } else if (otherAgentAction.getCellsUsed().contains(new Position(position.getCol(), position.getRow() + 1))) {
                                        agentActionCanBeMerged[i] = false;
                                        break thisAgent;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Command[] combinedAction = new Command[nAgents];

            for (int i = 0; i < nAgents; i++) {
                if (agentActionCanBeMerged[i] && container[i].size() != 0) {
                    Action agentAction = (Action) container[i].poll();
                    combinedAction[i] = agentAction.getCommand();
                } else {
                    combinedAction[i] = Command.NoOp;
                }
            }

            combinedActions.add(combinedAction);

            boolean allActionsAreMerged = true;
            for (int i = 0; i < container.length; i++) {
                if (!container[i].isEmpty()) {
                    allActionsAreMerged = false;
                    break;
                }
            }

            done = allActionsAreMerged;
        }

        return combinedActions.toArray(new Command[0][0]);
    }

    @Override
    public String toString() {
        return "cells-used";
    }

}
