package client.path;

import client.distance.LazyManhattan;
import client.state.*;

import java.util.Comparator;
import java.util.HashSet;
import java.util.PriorityQueue;

abstract class AbstractSearch {

    private Comparator<Node> comparator;
    private LazyManhattan manhattan;
    private int stateSize;

    AbstractSearch(Comparator<Node> comparator, int stateSize) {
        this.comparator = comparator;
        this.manhattan = new LazyManhattan();
        this.stateSize = stateSize;
    }

    public Node plan(State state, Position from, Position to) {
        return this.search(state, from, to, null, this.stateSize);
    }

    public Node plan(State state, Position from, Position to, Color color) {
        return this.search(state, from, to, color, this.stateSize);
    }

    public Node plan(State state, Position from, Position to, int maxDistance) {
        return this.search(state, from, to, null, maxDistance);
    }

    public Node plan(State state, Position from, Position to, Color color, int maxDistance) {
        return this.search(state, from, to, color, maxDistance);
    }

    protected Node search(State state, Position from, Position to, Color color, int maxDistance) {
        Level level = state.getLevel();
        HashSet<Node> explored = new HashSet<>();
        HashSet<Node> frontierSet = new HashSet<>();
        PriorityQueue<Node> frontier = new PriorityQueue<>(this.comparator);
        Node root = new Node(from, 0);
        frontier.add(root);
        frontierSet.add(root);
        while (!frontier.isEmpty()) {
            Node node = frontier.poll();
            Position position = node.getPosition();
            if (position.equals(to))
                return node;
            frontier.remove(node);
            explored.add(node);
            Position[] directions = new Position[] {
                    position.north(),
                    position.east(),
                    position.south(),
                    position.west()
            };
            for (Position direction : directions) {
                int h = this.manhattan.distance(state, direction, to);
                Node child = new Node(node, direction, h);
                if (node.g() <= maxDistance
                        && this.isFree(level, state, color, direction)
                        && this.isUndiscovered(explored, frontierSet, child)) {
                    frontier.add(child);
                    frontierSet.add(child);
                }
            }
        }
        return null;
    }

    protected abstract boolean isFree(Level level, State state, Color color, Position position);

    private boolean isUndiscovered(HashSet<Node> explored, HashSet<Node> frontier, Node child) {
        return !explored.contains(child) && !frontier.contains(child);
    }

}
