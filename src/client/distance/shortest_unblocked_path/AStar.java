package client.distance.shortest_unblocked_path;

import client.distance.LazyManhattan;
import client.state.*;

import java.util.HashSet;
import java.util.PriorityQueue;

class AStar {

    private NodeComparator comparator;
    private LazyManhattan manhattan;

    AStar() {
        this.comparator = new NodeComparator();
        this.manhattan = new LazyManhattan();
    }

    Node plan(State state, Position from, Position to) {
        return this.search(state, from, to, null);
    }

    Node plan(State state, Agent from, Position to) {
        Color color = from.getColor();
        return this.search(state, from.getPosition(), to, color);
    }

    Node plan(State state, Position from, Agent to) {
        return this.plan(state, to, from);
    }

    Node plan(State state, Agent from, Agent to) {
        return this.plan(state, from, to.getPosition());
    }

    private Node search(State state, Position from, Position to, Color color) {
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
                if (this.isFree(level, state, color, direction) && this.isUndiscovered(explored, frontierSet, child)) {
                    frontier.add(child);
                    frontierSet.add(child);
                }
            }
        }
        return null;
    }

    private boolean isFree(Level level, State state, Color color, Position position) {
        return !level.wallAt(position)
                && (!state.boxAt(position) || state.getBoxAt(position).getColor() == color)
                && !state.agentAt(position);
    }

    private boolean isUndiscovered(HashSet<Node> explored, HashSet<Node> frontier, Node child) {
        return !explored.contains(child) && !frontier.contains(child);
    }

}
