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
import java.util.Collections;

/**
 * Greedily merges actions.
 * Assumes that actions contains no NoOps.
 */
public class Greedy extends AMerger {

    public Command[][] merge(State initialState, Action[] actions) {
        Agent[] agents = initialState.getAgents();
        ArrayList<ArrayList<Action>> agentsActions = this.splitActionsToAgents(actions, agents);
        ArrayList<Action> remainingActions = new ArrayList<>();
        Collections.addAll(remainingActions, actions);
        ArrayList<ArrayList<Action>> jointActions = new ArrayList<>();
        this.createJointActions(initialState, agentsActions, remainingActions, jointActions);

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
            ArrayList<ArrayList<Action>> agentsActions,
            ArrayList<Action> actions,
            ArrayList<ArrayList<Action>> jointActions
    ) {
        if (actions.isEmpty()) {
            return;
        }

        Agent[] agents = state.getAgents();
        int nAgents = agents.length;
        for (int i = nAgents; 0 < i; i--) {
            // make top action with actions from the first i agents
            ArrayList<Action> jointAction = this.makeJointAction(state, agentsActions, i);
            ArrayList<Action> remainingActions = (ArrayList<Action>) actions.clone();
            for (Action action : jointAction) {
                remainingActions.remove(action);
            }
            // detect if this jointAction will lead to a solution or if there are conflicts
            boolean conflicts = this.detectMergeConflicts(state, jointAction, remainingActions);
            if (!conflicts) {
                // remove joint action from agents actions
                for (Action action : jointAction) {
                    agentsActions.get(action.getAgentID()).remove(action);
                }
                // add jointAction to jointActions
                jointActions.add(jointAction);
                state = StateGenerator.generate(state, jointAction);
                // call createJointAction again for next layer
                this.createJointActions(
                        state,
                        agentsActions,
                        remainingActions,
                        jointActions
                );
                return;
            }
        }

        // the order of agents did not match the order in the original plan
        // we solve this by creating a joint action that only contains the first action
        ArrayList<Action> remainingActions = (ArrayList<Action>) actions.clone();
        Action action = remainingActions.get(0);
        remainingActions.remove(action);
        agentsActions.get(action.getAgentID()).remove(action);
        ArrayList<Action> jointAction = new ArrayList<>();
        for (int i = 0; i < nAgents; i++) {
            Action noOp = this.makeNoOp(agents, i);
            jointAction.add(noOp);
        }
        jointAction.set(action.getAgentID(), action);
        jointActions.add(jointAction);
        state = StateGenerator.generate(state, jointAction);
        this.createJointActions(
                state,
                agentsActions,
                remainingActions,
                jointActions
        );
    }

    private boolean noActionsLeft(ArrayList<ArrayList<Action>> agentsActions) {
        for (ArrayList<Action> agentActions : agentsActions) {
            if (!agentActions.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private ArrayList<Action> makeJointAction(
            State state,
            ArrayList<ArrayList<Action>> agentsActions,
            int agentsToAdd
    ) {
        Agent[] agents = state.getAgents();
        ArrayList<Action> jointAction = new ArrayList<>(agents.length);

        int agentsAdded = 0;
        int nAgents = agentsActions.size();
        for (int agentID = 0; agentID < nAgents; agentID++) {
            if (agentsAdded < agentsToAdd) {
                ArrayList<Action> agentActions = agentsActions.get(agentID);
                if (agentActions.size() == 0) {
                    Action noOpAction = this.makeNoOp(agents, agentID);
                    jointAction.add(noOpAction);
                } else {
                    Action agentAction = agentActions.get(0);
                    jointAction.add(agentAction);
                    agentsAdded++;
                }
            } else {
                Action noOpAction = this.makeNoOp(agents, agentID);
                jointAction.add(noOpAction);
            }
        }
        return jointAction;
    }

    private Action makeNoOp(Agent[] agents, int agentID) {
        Position[] cellsUsed = new Position[] { agents[agentID].getPosition() };
        return new Action(agentID, Command.NoOp, cellsUsed);
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

    private ArrayList<ArrayList<Action>> splitActionsToAgents(Action[] actions, Agent[] agents) {
        ArrayList<ArrayList<Action>> agentsActions = new ArrayList<>(agents.length);
        for (int i = 0; i < agents.length; i++) {
            agentsActions.add(new ArrayList<>());
        }
        for (Action action : actions) {
            agentsActions.get(action.getAgentID()).add(action);
        }
        return agentsActions;
    }

}
