package client.heuristics.unblocker;

import client.state.Position;

import java.util.ArrayList;
import java.util.Collections;

public class PathNode {

    private int distance;
    private PathNode parent;
    private Position position;

    public PathNode(Position position) {
        this.parent = null;
        this.distance = 0;
        this.position = position;
    }

    public PathNode(PathNode parent, Position position) {
        this.parent = parent;
        this.position = position;
        this.distance = parent.distance + 1;
    }

    public int getDistance() {
        return distance;
    }

    public Position getPosition() {
        return position;
    }

    public boolean isRoot() {
        return this.parent == null;
    }

    public ArrayList<Position> getPath(boolean reverse) {
        ArrayList<Position> path = new ArrayList<>();
        PathNode node = this;
        while (node != null) {
            path.add(node.getPosition());
            node = node.parent;
        }
        if (reverse) {
            Collections.reverse(path);
        }
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
        PathNode other = (PathNode) obj;
        return other.position.equals(this.position);
    }

    @Override
    public int hashCode() {
        return this.position.hashCode();
    }

}
