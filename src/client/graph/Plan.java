package client.graph;

import client.definitions.AHeuristic;
import client.state.*;

import java.sql.Time;
import java.util.*;

public class Plan {

    private State state;
    private Plan parent;
    private int time;
    private Action action;
    private int _hash;

    /**
     * Constructs a root node.
     *
     * @param initialState State of the root.
     */
    public Plan(State initialState) {
        this.state = initialState;
        this.parent = null;
        this.time = 0;
        this.action = null;
        this._hash = this.computeHashCode();
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
        this.state = state;
        this.parent = parent;
        this.time = time;
        this.action = action;
        this._hash = this.computeHashCode();
    }

    public Action getAction() {
        return action;
    }

    public int g() {
        return this.time;
    }

    public int f() {
        return this.time + this.state.h();
    }

    public int h() {
        return this.state.h();
    }

    public int getTime() {
        return time;
    }

    public State getState() {
        return state;
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
            cs.addAll(constraints.get(this.time + 1));
            cs.addAll(constraints.get(this.time));
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

    private int computeHashCode() {
        return this.state.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (obj.getClass() != this.getClass())
            return false;
        Plan other = (Plan) obj;
        return other.getState().equals(this.state);
    }

    @Override
    public int hashCode() {
        return this._hash;
    }
}
