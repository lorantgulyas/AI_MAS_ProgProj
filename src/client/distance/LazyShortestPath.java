package client.distance;

import client.definitions.ADistance;
import client.state.Position;
import client.state.State;

import java.util.HashMap;

public class LazyShortestPath extends ADistance {
    private HashMap<PositionPair, Integer> D;
    private BFS bfs;

    public LazyShortestPath(State state) {
        this.D = new HashMap<>();
        this.bfs = new BFS(state);
    }

    public int distance(Position p1, Position p2) {
        PositionPair pair = new PositionPair(p1, p2);
        int distance = this.D.getOrDefault(pair, -1);
        if (distance == -1) {
            distance = this.search(p1, p2, pair);
        }
        return distance;
    }

    private synchronized int search(Position p1, Position p2, PositionPair pair) {
        int distance = this.bfs.search(p1, p2);
        this.D.put(pair, distance);
        return distance;
    }

    public int getV() {
        return this.bfs.getV();
    }

    @Override
    public String toString() {
        return "shortest-path";
    }

    class PositionPair {
        public Position p1;
        public Position p2;

        private int _hash;

        public PositionPair(Position p1, Position p2) {
            this.p1 = p1;
            this.p2 = p2;
            this._hash = p1.hashCode() * p2.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            PositionPair other = (PositionPair) obj;
            // must respect symmetry
            return (this.p1.equals(other.p1) && this.p2.equals(other.p2))
                    || (this.p2.equals(other.p1) && this.p1.equals(other.p2));
        }

        @Override
        public int hashCode() {
            return this._hash;
        }
    }
}
