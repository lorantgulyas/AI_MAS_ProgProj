package client.heuristics.unblocker;

import client.definitions.ADistance;
import client.state.Agent;
import client.state.Box;
import client.state.State;

public class Block {

    private Box box;
    private Agent responsible;
    private int value;
    private int _hash;

    public Block(Agent responsible, int value) {
        this.responsible = responsible;
        this.value = value;
        this._hash = this.computeHashCode();
    }

    public Block(Box what, Agent responsible, int value) {
        this.box = what;
        this.responsible = responsible;
        this.value = value;
        this._hash = this.computeHashCode();
    }

    public Box getBox() {
        return box;
    }

    public Agent getResponsible() {
        return responsible;
    }

    public int getValue() {
        return value;
    }

    public int h(State state, ADistance measurer) {
        int h = this.value;
        if (this.responsible != null && this.box != null) {
            // important to multiply to ensure that an agent will unblock before anything else
            h += this.value * measurer.distance(state, this.responsible.getPosition(), this.box.getPosition());
        }
        return h;
    }

    private int computeHashCode() {
        int hashCode = (value + 1);
        hashCode = this.responsible == null ? hashCode : hashCode * responsible.hashCode();
        hashCode = this.box == null ? hashCode : hashCode * this.box.hashCode();
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null)
            return false;
        if (obj.getClass() != this.getClass())
            return false;
        Block other = (Block) obj;
        return other.value == this.value
                && (
                    (other.responsible == null && this.responsible == null)
                    || (other.responsible != null && this.responsible != null && other.responsible.getId() == this.responsible.getId())
                )
                && (
                    (other.box == null && this.box == null)
                    || (other.box != null && this.box != null && other.box.equals(this.box))
                );
    }

    @Override
    public int hashCode() {
        return this._hash;
    }

    @Override
    public String toString() {
        String box = this.box == null ? "-" : "(" + this.box.getId() + "," + this.box.getPosition().toString() + ")";
        String responsible = this.responsible == null ? "-" : "(" + this.responsible.getId() + "," + this.responsible.getPosition().toString() + ")";
        return "(" + box + "," + responsible + "," + this.value + ")";
    }

    public String toString(State state, ADistance measurer) {
        String box = this.box == null ? "-" : "(" + this.box.getId() + "," + this.box.getPosition().toString() + ")";
        String responsible = this.responsible == null ? "-" : "(" + this.responsible.getId() + "," + this.responsible.getPosition().toString() + ")";
        int h = this.h(state, measurer);
        return "(" + box + "," + responsible + "," + this.value + "," + h + ")";
    }

}
