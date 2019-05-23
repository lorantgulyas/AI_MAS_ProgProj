package client.distance.shortest_unblocked_path;

import client.state.Position;

class Node {

    private int g;
    private int h;
    private Position position;

    Node(Position position) {
        this.g = 0;
        this.h = 0;
        this.position = position;
    }

    Node(Position position, int h) {
        this.g = 0;
        this.h = h;
        this.position = position;
    }

    Node(Node parent, Position position) {
        this.g = parent.g() + 1;
        this.h = 0;
        this.position = position;
    }

    Node(Node parent, Position position, int h) {
        this.g = parent.g() + 1;
        this.h = h;
        this.position = position;
    }

    int f() {
        return this.g + this.h;
    }

    int g() {
        return this.g;
    }

    Position getPosition() {
        return this.position;
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
