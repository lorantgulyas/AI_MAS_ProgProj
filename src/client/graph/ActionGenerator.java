package client.graph;

import client.state.Agent;
import client.state.Position;
import client.state.State;
import client.utils.ConflictDetector;

import java.util.*;

public class ActionGenerator {

    /**
     * Generates all the possible joint actions that can be be performed in the given state.
     *
     * @param currentState The current state to act int.
     * @return List of possible joint actions.
     */
    public static ArrayList<ArrayList<Action>> children(State currentState) {
        // find all actions that each agent can perform individually
        Agent[] agents = currentState.getAgents();
        ArrayList<ArrayList<Action>> agentsActions = new ArrayList<>(agents.length);
        for (int i = 0; i < agents.length; i++) {
            agentsActions.add(ActionGenerator.children(currentState, i));
        }

        return ActionGenerator.createJointActions(agentsActions);
    }

    private static ArrayList<ArrayList<Action>> createJointActions(ArrayList<ArrayList<Action>> agentsActions) {
        int nAgents = agentsActions.size();

        // find total number of jointActions
        int nJointActions = 1;
        for (ArrayList<Action> agentActions : agentsActions) {
            nJointActions *= agentActions.size();
        }

        // initialize values used to find which action to pick
        int[] counts = new int[nAgents];
        int[] indices = new int[nAgents];
        int[] updateIndexEvery = new int[nAgents];

        Arrays.fill(counts, 0);
        Arrays.fill(indices, 0);

        int update = 1;
        for (int i = nAgents - 1; -1 < i; i--) {
            updateIndexEvery[i] = update;
            update *= agentsActions.get(i).size();
        }

        // make joint actions
        ArrayList<ArrayList<Action>> jointActions = new ArrayList<>(nJointActions);
        for (int i = 0; i < nJointActions; i++) {
            ArrayList<Action> jointAction = new ArrayList<>(nAgents);
            for (int j = 0; j < nAgents; j++) {
                int index = indices[j];
                Action action = agentsActions.get(j).get(index);
                jointAction.add(action);

                // update values to keep track of which action to pick next for this agent
                int count = counts[j];
                int updateEvery = updateIndexEvery[j];
                int nextCount = count + 1;
                counts[j] = nextCount;
                if (nextCount % updateEvery == 0) {
                    indices[j] = (index + 1) % agentsActions.get(j).size();
                }
            }
            // only add actions without conflicts
            if (!ConflictDetector.conflict(jointAction)) {
                jointActions.add(jointAction);
            }
        }

        return jointActions;
    }

    /**
     * Generates all the possible actions an agent can perform in the given state.
     *
     * @param currentState The current state to act in.
     * @param agentID The agent that is acting.
     * @return The list of possible actions.
     */
    public static ArrayList<Action> children(State currentState, int agentID) {
        ArrayList<Action> children = new ArrayList<>();
        Agent agent = currentState.getAgents()[agentID];
        Position agentPos = agent.getPosition();
        for (Command command : Command.EVERY) {
            switch (command.actionType) {
                case NoOp:
                    Position[] noOpCells = new Position[] { agentPos };
                    Action noOpAction = new Action(agentID, Command.NoOp, noOpCells);
                    children.add(noOpAction);
                    break;
                case Move:
                    Position moveTo = agentPos.go(command.dir1);
                    if (currentState.isFree(moveTo)) {
                        Position[] moveCells = new Position[] { moveTo };
                        //Position[] moveCells = new Position[] { agentPos, moveTo };
                        Action moveAction = new Action(agentID, command, moveCells);
                        children.add(moveAction);
                    }
                    break;
                case Pull:
                    Position pullFrom = agentPos.go(command.dir2);
                    Position pullTo = agentPos.go(command.dir1);
                    if (ActionGenerator.canMoveBox(currentState, pullFrom, pullTo, agent)) {
                        Position[] pullCells = new Position[] { agentPos, pullTo };
                        //Position[] pullCells = new Position[] { agentPos, pullFrom, pullTo };
                        Action pullAction = new Action(agentID, command, pullCells);
                        children.add(pullAction);
                    }
                    break;
                case Push:
                    Position pushFrom = agentPos.go(command.dir1);
                    Position pushTo = pushFrom.go(command.dir2);
                    if (ActionGenerator.canMoveBox(currentState, pushFrom, pushTo, agent)) {
                        Position[] pushCells = new Position[] { pushFrom, pushTo };
                        //Position[] pushCells = new Position[] { agentPos, pushFrom, pushTo };
                        Action pushAction = new Action(agentID, command, pushCells);
                        children.add(pushAction);
                    }
                    break;
            }
        }
        Collections.shuffle(children);
        return children;
    }

    /**
     * Generates all the possible actions an agent can perform in the current state under some constraints.
     *
     * @param currentState The current state to act in.
     * @param constraints The set of positions that cannot be accessed by an action.
     * @param agentID The agent that is acting.
     * @return The list of possible actions.
     */
    public static ArrayList<Action> constrainedChildren(State currentState, Set<Position> constraints, int agentID) {
        ArrayList<Action> children = new ArrayList<>();
        Agent agent = currentState.getAgents()[agentID];
        Position agentPos = agent.getPosition();
        for (Command command : Command.EVERY) {
            switch (command.actionType) {
                case NoOp:
                    if (!constraints.contains(agentPos)) {
                        Position[] noOpCells = new Position[] { agentPos };
                        Action noOpAction = new Action(agentID, Command.NoOp, noOpCells);
                        children.add(noOpAction);
                    }
                    break;
                case Move:
                    Position moveTo = agentPos.go(command.dir1);
                    if (currentState.isFree(moveTo) && !constraints.contains(moveTo)) {
                        Position[] moveCells = new Position[] { moveTo };
                        //Position[] moveCells = new Position[] { agentPos, moveTo };
                        Action moveAction = new Action(agentID, command, moveCells);
                        children.add(moveAction);
                    }
                    break;
                case Pull:
                    Position pullFrom = agentPos.go(command.dir2);
                    Position pullTo = agentPos.go(command.dir1);
                    if (ActionGenerator.canMoveBoxConstrained(currentState, constraints, pullFrom, pullTo, agent)) {
                        Position[] pullCells = new Position[] { agentPos, pullTo };
                        //Position[] pullCells = new Position[] { agentPos, pullFrom, pullTo };
                        Action pullAction = new Action(agentID, command, pullCells);
                        children.add(pullAction);
                    }
                    break;
                case Push:
                    Position pushFrom = agentPos.go(command.dir1);
                    Position pushTo = pushFrom.go(command.dir2);
                    if (ActionGenerator.canMoveBoxConstrained(currentState, constraints, pushFrom, pushTo, agent)) {
                        Position[] pushCells = new Position[] { pushFrom, pushTo };
                        //Position[] pushCells = new Position[] { agentPos, pushFrom, pushTo };
                        Action pushAction = new Action(agentID, command, pushCells);
                        children.add(pushAction);
                    }
                    break;
            }
        }

        Collections.shuffle(children);
        return children;
    }

    public static boolean canMoveBox(State currentState, Position from, Position to, Agent agent) {
        return currentState.boxAt(from)
                && currentState.isFree(to)
                && currentState.getBoxAt(from).getColor() == agent.getColor();
    }

    public static boolean canMoveBoxConstrained(State currentState, Set<Position> constraints, Position from, Position to, Agent agent) {
        return currentState.boxAt(from)
                && currentState.isFree(to)
                && !constraints.contains(from)
                && !constraints.contains(to)
                && currentState.getBoxAt(from).getColor() == agent.getColor();
    }

}
