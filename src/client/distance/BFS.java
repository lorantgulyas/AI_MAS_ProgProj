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
        HashSet<Position> frontierSet = new HashSet<>();
        HashSet<Position> explored = new HashSet<>();
        frontier.add(new BFSNode(0, start));
        frontierSet.add(start);
        while (!frontierSet.isEmpty()) {
            BFSNode node = frontier.pop();
            Position pos = node.position;
            frontierSet.remove(pos);
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

            if (!explored.contains(north) && !frontierSet.contains(north) && this.position2vertex.containsKey(north)) {
                frontier.add(northNode);
                frontierSet.add(north);
            }
            if (!explored.contains(east) && !frontierSet.contains(east) && this.position2vertex.containsKey(east)) {
                frontier.add(eastNode);
                frontierSet.add(east);
            }
            if (!explored.contains(south) && !frontierSet.contains(south) && this.position2vertex.containsKey(south)) {
                frontier.add(southNode);
                frontierSet.add(south);
            }
            if (!explored.contains(west) && !frontierSet.contains(west) && this.position2vertex.containsKey(west)) {
                frontier.add(westNode);
                frontierSet.add(west);
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

        @Override
        public int hashCode() {
            return this.position.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (obj.getClass() != this.getClass())
                return false;
            BFSNode other = (BFSNode) obj;
            return this.position.equals(other.position);
        }
    }
}
