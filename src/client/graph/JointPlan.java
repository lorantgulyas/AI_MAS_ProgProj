package client.graph;

import client.definitions.AHeuristic;
import client.state.State;

import java.util.ArrayList;
import java.util.Collections;

public class JointPlan extends AbstractPlan {

    private JointPlan parent;
    private ArrayList<Action> actions;

    /**
     * Constructs a root node.
     *
     * @param initialState State of the root.
     */
    public JointPlan(State initialState) {
        super(initialState);
        this.actions = null;
        this.parent = null;
    }

    /**
     * Constructs a non-root node.
     *
     * @param state State of the node.
     * @param parent Parent of the node.
     * @param time The depth of the node in the tree.
     * @param actions The actions that were used to generate the node from its parent.
     */
    public JointPlan(State state, JointPlan parent, int time, ArrayList<Action> actions) {
        super(state, time);
        this.actions = actions;
        this.parent = parent;
    }

    public ArrayList<Action> getActions() {
        return this.actions;
    }

    public JointPlan getParent() {
        return this.parent;
    }

    public boolean isInitialState() {
        return this.parent == null;
    }

    /**
     * Extracts all the parent nodes on the path from the root to this node.
     * @return List of nodes on path from this node to the root
     */
    public ArrayList<ArrayList<Action>> extract() {
        JointPlan plan = this;
        ArrayList<ArrayList<Action>> jointActions = new ArrayList<>();
        while (!plan.isInitialState()) {
            jointActions.add(plan.getActions());
            plan = plan.getParent();
        }
        Collections.reverse(jointActions);
        return jointActions;
    }

    /**
     * Generates the list of child nodes from the current state for a given agent.
     *
     * @return List of child nodes.
     */
    public ArrayList<JointPlan> getChildren(AHeuristic heuristic) {
        ArrayList<JointPlan> children = new ArrayList<>();
        ArrayList<ArrayList<Action>> jointActions = ActionGenerator.children(this.state);
        for (ArrayList<Action> jointAction: jointActions) {
            State childState = StateGenerator.generate(this.state , jointAction);
            int h = heuristic.h(childState);
            childState.setH(h);
            JointPlan child = new JointPlan(childState, this, this.time + 1, jointAction);
            children.add(child);
        }
        return children;
    }

}
