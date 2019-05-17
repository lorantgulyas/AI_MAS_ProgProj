package client.mergers;

import client.definitions.AMerger;
import client.graph.Action;
import client.graph.Command;
import client.graph.StateGenerator;
import client.state.Agent;
import client.state.Position;
import client.state.State;
import client.utils.ConflictDetector;

import java.util.ArrayList;
import java.util.LinkedList;

public class Greedy extends AMerger {

    public Command[][] merge(State initialState, Action[] actions) {
        Agent[] agents = initialState.getAgents();
        ArrayList<LinkedList<Action>> agentsActions = this.splitActionsToAgents(actions, agents);
        ArrayList<ArrayList<Action>> jointActions = new ArrayList<>();
        ArrayList<Action> remainingActions = new ArrayList<>();
        for (Action action : actions) {
            remainingActions.add(action);
        }
        this.createJointActions(initialState, remainingActions, agentsActions, 0, jointActions);

        // extract commands
        int nAgents = agents.length;
        int nActions = jointActions.size();
        Command[][] plan = new Command[nActions][nAgents];
        for (int i = 0; i < nActions; i++) {
            ArrayList<Action> jointAction = jointActions.get(i);
            for (int j = 0; j < nAgents; j++) {
                plan[i][j] = jointAction.get(j).getCommand();
            }
        }
        return plan;
    }

    @Override
    public String toString() {
        return "greedy";
    }

    private void createJointActions(
            State state,
            ArrayList<Action> remainingActions,
            ArrayList<LinkedList<Action>> agentsActions,
            int jointActionLevel,
            ArrayList<ArrayList<Action>> jointActions
    ) {
        if (remainingActions.isEmpty()) {
            return;
        }

        Agent[] agents = state.getAgents();
        for (int i = agents.length; 0 < i; i--) {
            // make top action with actions from the first i agents
            Action firstAction = remainingActions.get(0);
            ArrayList<Action> jointAction = this.makeJointAction(state, agentsActions, i, jointActionLevel, firstAction);
            ArrayList<Action> candidateRemainingActions = (ArrayList<Action>) remainingActions.clone();
            for (Action action : jointAction) {
                candidateRemainingActions.remove(action);
            }
            // detect if this jointAction will lead to a solution or if there are conflicts
            boolean conflicts = this.detectMergeConflicts(state, jointAction, candidateRemainingActions);
            if (!conflicts) {
                // add jointAction to jointActions
                jointActions.add(jointAction);
                state = StateGenerator.generate(state, jointAction);
                // call createJointAction again for next layer
                this.createJointActions(
                        state,
                        candidateRemainingActions,
                        agentsActions,
                        jointActionLevel + 1,
                        jointActions
                );
            }
        }
    }

    private ArrayList<Action> makeJointAction(
            State state,
            ArrayList<LinkedList<Action>> agentsActions,
            int agentsToAdd,
            int jointActionLevel,
            Action firstAction
    ) {
        Agent[] agents = state.getAgents();
        ArrayList<Action> jointAction = new ArrayList<>(agents.length);
        for (int i = 0; i < agents.length; i++) {
            jointAction.add(null);
        }

        int firstActionAgentID = firstAction.getAgentID();
        jointAction.set(firstActionAgentID, firstAction);
        int agentsAdded = 1;

        int nAgents = agentsActions.size();
        for (int agentID = 0; agentID < nAgents; agentID++) {
            if (agentID != firstActionAgentID) {
                if (agentsToAdd <= agentsAdded ) {
                    Position[] cellsUsed = new Position[] { agents[agentID].getPosition() };
                    Action noOpAction = new Action(agentID, Command.NoOp, cellsUsed);
                    jointAction.set(agentID, noOpAction);
                } else {
                    LinkedList<Action> agentActions = agentsActions.get(agentID);
                    if (jointActionLevel < agentActions.size()) {
                        Action agentAction = agentActions.get(jointActionLevel);
                        jointAction.set(agentID, agentAction);
                        agentsAdded++;
                    } else {
                        Position[] cellsUsed = new Position[] { agents[agentID].getPosition() };
                        Action noOpAction = new Action(agentID, Command.NoOp, cellsUsed);
                        jointAction.set(agentID, noOpAction);
                    }
                }
            }
        }
        return jointAction;
    }

    private boolean detectMergeConflicts(State state, ArrayList<Action> jointAction, Iterable<Action> remainingActions) {
        if (ConflictDetector.conflict(state, jointAction)) {
            return true;
        }
        state = StateGenerator.generate(state, jointAction);
        for (Action action : remainingActions) {
            if (ConflictDetector.conflict(state, action)) {
                return true;
            }
            state = StateGenerator.generate(state, action);
        }
        return !state.isGoalState();
    }

    private ArrayList<LinkedList<Action>> splitActionsToAgents(Action[] actions, Agent[] agents) {
        ArrayList<LinkedList<Action>> container = new ArrayList<>(agents.length);

        for (int i = 0; i < agents.length; i++) {
            LinkedList<Action> myActions = new LinkedList<>();
            for (Action action : actions) {
                if (action.getAgentID() == i) {
                    myActions.add(action);
                }
            }
            container.add(myActions);
        }
        return container;
    }

}
