package client.graph;

import client.definitions.AHeuristic;
import client.state.*;

import java.sql.Time;
import java.util.*;

abstract class AbstractPlan {

    protected State state;

    protected int time;
    protected int _hash;

    /**
     * Constructs a root node.
     *
     * @param initialState State of the root.
     */
    public AbstractPlan(State initialState) {
        this.state = initialState;
        this.time = 0;
        this._hash = this.computeHashCode();
    }

    /**
     * Constructs a non-root node.
     *
     * @param state State of the node.
     * @param time The depth of the node in the tree.
     */
    public AbstractPlan(State state, int time) {
        this.state = state;
        this.time = time;
        this._hash = this.computeHashCode();
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
        return this.time;
    }

    public State getState() {
        return this.state;
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
        AbstractPlan other = (AbstractPlan) obj;
        return other.getState().equals(this.state);
    }

    @Override
    public int hashCode() {
        return this._hash;
    }
}
