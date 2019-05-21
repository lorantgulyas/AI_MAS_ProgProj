package client.heuristics.unblocker;

import client.state.Agent;
import client.state.Box;

public class Block {

    private Box box;
    private Agent responsible;
    private int value;

    public Block(Agent responsible, int value) {
        this.responsible = responsible;
        this.value = value;
    }

    public Block(Box what, Agent responsible, int value) {
        this.box = what;
        this.responsible = responsible;
        this.value = value;
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
}
