package client.graph;

import client.definitions.AHeuristic;
import client.state.*;

import java.util.*;

public class Plan extends AbstractPlan {

    private Action action;
    protected Plan parent;

    /**
     * Constructs a root node.
     *
     * @param initialState State of the root.
     */
    public Plan(State initialState) {
        super(initialState);
        this.action = null;
        this.parent = null;
    }

    /**
     * Constructs a non-root node.
     *
     * @param state State of the node.
     * @param parent Parent of the node.
     * @param time The depth of the node in the tree.
     * @param action The action that was used to generate the node from its parent.
     */
    public Plan(State state, Plan parent, int time, Action action) {
        super(state, time);
        this.action = action;
        this.parent = parent;
    }

    public Action getAction() {
        return action;
    }

    public Plan getParent() {
        return this.parent;
    }

    public boolean isInitialState() {
        return this.parent == null;
    }

    /**
     * Extracts all the parent nodes on the path from the root to this node.
     * @return List of nodes on path from this node to the root
     */
    public Action[] extract() {
        Plan plan = this;
        ArrayList<Action> actions = new ArrayList<>();
        while (!plan.isInitialState()) {
            actions.add(plan.getAction());
            plan = plan.parent;
        }
        Collections.reverse(actions);
        return actions.toArray(new Action[0]);
    }

    /**
     * Generates the list of child nodes from the current state for a given agent.
     *
     * @param agentID The agent ID.
     * @return List of child nodes.
     */
    public ArrayList<Plan> getChildren(AHeuristic heuristic, int agentID) {
        ArrayList<Plan> children = new ArrayList<>();
        ArrayList<Action> actions = ActionGenerator.children(this.state, agentID);
        for (Action action : actions) {
            State childState = StateGenerator.generate(this.state , action);
            int h = heuristic.h(childState);
            childState.setH(h);
            Plan child = new Plan(childState, this, this.time + 1, action);
            children.add(child);
        }
        return children;
    }

    /**
     * Generates the list of child nodes from the current state for a given agent under a set of constraints.
     *
     * @param agentID The agent ID.
     * @param constraints List of Position sets. The ith entry in the list is the set of cells that cannot be accessed at time i + 1.
     * @param previousActions List of actions performed by other agents. Must be used to generate a different state in case of a NoOp.
     * @return List of child nodes.
     */
    public ArrayList<Plan> getConstrainedChildren(AHeuristic heuristic, int agentID, ArrayList<Set<Position>> constraints, ArrayList<ArrayList<Action>> previousActions) {
        State partialNextState = this.getPartialNextState(previousActions);
        ArrayList<Plan> children = new ArrayList<>();
        ArrayList<Action> actions = this.getActions(this.state, constraints, agentID);
        for (Action action : actions) {
            State childState = StateGenerator.generate(partialNextState, action);
            int h = heuristic.h(childState);
            childState.setH(h);
            Plan child = new Plan(childState, this, this.time + 1, action);
            children.add(child);
        }
        return children;
    }

    private ArrayList<Action> getActions(State currentState, ArrayList<Set<Position>> constraints, int agentID) {
        int n = constraints.size();
        if (n - 1 < this.time) {
            return ActionGenerator.children(currentState, agentID);
        } else if (n - 1 < this.time + 1) {
            return ActionGenerator.constrainedChildren(currentState, constraints.get(this.time), agentID);
        } else {
            HashSet cs = new HashSet();
            cs.addAll(constraints.get(this.time));
            cs.addAll(constraints.get(this.time + 1));
            return ActionGenerator.constrainedChildren(currentState, cs, agentID);
        }
    }

    private State getPartialNextState(ArrayList<ArrayList<Action>> previousActions) {
        if (previousActions.size() - 1 < this.time) {
            return this.state;
        } else {
            return StateGenerator.generate(this.state, previousActions.get(this.time));
        }
    }
}
