package client.path;

import client.state.Position;

import java.util.ArrayList;
import java.util.Collections;

public class Node {

    private int g;
    private int h;
    private Node parent;
    private Position position;

    Node(Position position, int h) {
        this.g = 0;
        this.h = h;
        this.parent = null;
        this.position = position;
    }

    Node(Node parent, Position position, int h) {
        this.g = parent.g() + 1;
        this.h = h;
        this.parent = parent;
        this.position = position;
    }

    public int f() {
        return this.g + this.h;
    }

    public int g() {
        return this.g;
    }

    public int h() {
        return this.h;
    }

    public Position getPosition() {
        return this.position;
    }

    public ArrayList<Position> path() {
        ArrayList<Position> path = new ArrayList<>();
        Node node = this;
        while (node != null) {
            path.add(node.getPosition());
            node = node.parent;
        }
        Collections.reverse(path);
        return path;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null)
            return false;
        if (obj.getClass() != this.getClass())
            return false;
        Node other = (Node) obj;
        return other.position.equals(this.position);
    }

    @Override
    public int hashCode() {
        return this.position.hashCode();
    }

}
