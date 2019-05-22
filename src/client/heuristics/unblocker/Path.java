package client.heuristics.unblocker;

import client.state.*;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;

public class Path {

    private boolean[][] walls;
    private ArrayList<Position> path;

    public Path(State initialState, Goal goal, Box box, Agent agent) {
        this.walls = initialState.getLevel().getWalls();
        ArrayList<Position> agentPath = this.bfs(agent.getPosition(), box.getPosition());
        ArrayList<Position> goalPath = this.bfs(box.getPosition(), goal.getPosition());
        goalPath.remove(box.getPosition());
        agentPath.addAll(goalPath);
        this.path = agentPath;
    }

    public ArrayList<Position> getPath() {
        return path;
    }

    public ArrayList<Position> bfs(Position from, Position to) {
        HashSet<PathNode> explored = new HashSet<>();
        ArrayDeque<PathNode> frontier = new ArrayDeque<>();
        HashSet<PathNode> frontierSet = new HashSet<>();
        PathNode root = new PathNode(from);
        frontier.add(root);
        frontierSet.add(root);
        while (!frontier.isEmpty()) {
            PathNode node = frontier.pop();
            frontier.remove(node);
            explored.add(node);

            if (node.getPosition().equals(to)) {
                return node.getPath();
            }

            Position position = node.getPosition();
            PathNode north = new PathNode(node, position.north());
            PathNode east = new PathNode(node, position.east());
            PathNode south = new PathNode(node, position.south());
            PathNode west = new PathNode(node, position.west());
            PathNode[] directions = new PathNode[] { north, east, south, west };
            for (PathNode direction : directions) {
                if (this.canAddToBFSFrontier(explored, frontierSet, direction)) {
                    frontier.add(direction);
                    frontierSet.add(direction);
                }
            }
        }

        // this should be impossible since we have split the levels into separate closed rooms
        return new ArrayList<>();
    }

    public boolean blocked(State state, Agent agent, Position from, Position to, int maxDistance) {
        HashSet<PathNode> explored = new HashSet<>();
        ArrayDeque<PathNode> frontier = new ArrayDeque<>();
        HashSet<PathNode> frontierSet = new HashSet<>();
        PathNode root = new PathNode(from);
        frontier.add(root);
        frontierSet.add(root);
        while (!frontier.isEmpty()) {
            PathNode node = frontier.pop();
            frontier.remove(node);
            explored.add(node);

            if (node.getPosition().equals(to)) {
                return false;
            }

            Position position = node.getPosition();
            PathNode north = new PathNode(node, position.north());
            PathNode east = new PathNode(node, position.east());
            PathNode south = new PathNode(node, position.south());
            PathNode west = new PathNode(node, position.west());
            PathNode[] directions = new PathNode[] { north, east, south, west };
            for (PathNode direction : directions) {
                if (this.canAddToBlockedFrontier(explored, frontierSet, state, agent, maxDistance, direction)) {
                    frontier.add(direction);
                    frontierSet.add(direction);
                }
            }
        }

        return true;
    }

    private boolean canAddToBFSFrontier(HashSet<PathNode> explored, HashSet<PathNode> frontier, PathNode node) {
        return !this.hasWall(node)
                && !frontier.contains(node)
                && !explored.contains(node);
    }

    private boolean canAddToBlockedFrontier(
            HashSet<PathNode> explored,
            HashSet<PathNode> frontier,
            State state,
            Agent agent,
            int maxDistance,
            PathNode node
    ) {
        Position position = node.getPosition();
        Box nodeBox = state.getBoxAt(position);
        return node.getDistance() < maxDistance
                && !this.hasWall(node)
                && !frontier.contains(node)
                && !explored.contains(node)
                && (nodeBox == null || nodeBox.getColor() == agent.getColor())
                && !state.agentAt(position);
    }

    private boolean hasWall(PathNode node) {
        Position position = node.getPosition();
        return walls[position.getCol()][position.getRow()];
    }

}
