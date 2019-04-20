package client.distance;

import client.state.Level;
import client.state.Position;
import client.state.State;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class BFS{
    private int V;
    private HashMap<Position, Integer> position2vertex;
    private ArrayList<Position> vertex2position;

    public BFS(State state) {
        // count number of non-wall objects and create vertex maps
        this.position2vertex = new HashMap<>();
        this.vertex2position = new ArrayList<>();
        this.V = 0;
        Level level = state.getLevel();
        boolean[][] walls = level.getWalls();
        for (int i = 0; i < walls.length; i++) {
            boolean[] row = walls[i];
            for (int j = 0; j < row.length; j++) {
                if (!row[j]) {
                    Position position = new Position(i, j);
                    this.position2vertex.put(position, this.V);
                    this.vertex2position.add(position);
                    this.V++;
                }
            }
        }
    }

    public int search(Position start, Position end) {
        ArrayDeque<BFSNode> frontier = new ArrayDeque<>();
        HashSet<Position> explored = new HashSet<>();
        frontier.add(new BFSNode(0, start));
        while (!frontier.isEmpty()) {
            BFSNode node = frontier.pop();
            Position pos = node.position;
            explored.add(pos);
            if (pos.equals(end)) {
                return node.distance;
            }
            Position north = pos.north();
            Position east = pos.east();
            Position south = pos.south();
            Position west = pos.west();

            BFSNode northNode = new BFSNode(node.distance + 1, north);
            BFSNode eastNode = new BFSNode(node.distance + 1, east);
            BFSNode southNode = new BFSNode(node.distance + 1, south);
            BFSNode westNode = new BFSNode(node.distance + 1, west);

            if (!explored.contains(north) && this.position2vertex.containsKey(north)) {
                frontier.add(northNode);
            }
            if (!explored.contains(east) && this.position2vertex.containsKey(east)) {
                frontier.add(eastNode);
            }
            if (!explored.contains(south) && this.position2vertex.containsKey(south)) {
                frontier.add(southNode);
            }
            if (!explored.contains(west) && this.position2vertex.containsKey(west)) {
                frontier.add(westNode);
            }
        }

        // return MAX_VALUE corresponding to "infinity"
        // meaning that there are no paths between start and end
        return Integer.MAX_VALUE / this.V;
    }

    public int getV() {
        return V;
    }

    class BFSNode {
        public int distance;
        public Position position;

        public BFSNode(int distance, Position position) {
            this.distance = distance;
            this.position = position;
        }
    }
}
