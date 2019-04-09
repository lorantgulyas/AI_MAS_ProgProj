package client.heuristics;

import client.definitions.AHeuristic;
import client.state.*;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class SingleTasker extends AHeuristic {
    private Measurer measurer;

    public SingleTasker(State initialState) {
        super(initialState);
        this.measurer = new Measurer(initialState);
    }

    private Box getClosestBoxToGoal(ArrayList<Box> boxes, Goal goal) {
        Box closest = null;
        int minDistance = Integer.MAX_VALUE;
        int n = boxes.size();
        for (int i = 0; i < n; i++) {
            Box box = boxes.get(i);
            if (box.getLetter() == goal.getLetter()) {
                int distance = this.measurer.distance(box.getPosition(), goal.getPosition());
                if (distance < minDistance) {
                    closest = box;
                    minDistance = distance;
                }
            }
        }
        return closest;
    }

    private ArrayList<Goal> getUnfulfilledGoals(State state) {
        Goal[] goals = state.getGoals();
        ArrayList<Goal> unfulfilled = new ArrayList<>();
        for (int i = 0; i < goals.length; i++) {
            Goal goal = goals[i];
            Box box = state.getBoxAt(goal.getPosition());
            if (box != null && box.getLetter() != goal.getLetter()) {
                unfulfilled.add(goal);
            }
        }
        return unfulfilled;
    }

    private ArrayList<Box> getUnfinishedBoxes(State state) {
        ArrayList<Box> unfulfilled = new ArrayList<>();
        Box[] boxes = state.getBoxes();
        Goal[] goals = state.getGoals();
        for (Box box : boxes) {
            for (Goal goal : goals) {
                if (box.getLetter() != goal.getLetter()) {
                    unfulfilled.add(box);
                }
            }
        }
        return unfulfilled;
    }

    public int h(State n) {
        ArrayList<Goal> goals = this.getUnfulfilledGoals(n);
        int nGoals = goals.size();
        if (nGoals == 0) {
            return 0;
        }
        ArrayList<Box> boxes = this.getUnfinishedBoxes(n);
        // TODO: enable multi agent support!
        Agent agent = n.getAgents()[0];
        int sum = 0;
        int minAgent2BoxDistance = Integer.MAX_VALUE;
        int minWalkDistance = Integer.MAX_VALUE;
        for (int i = 0; i < nGoals; i++) {
            Goal goal = goals.get(i);
            Box box = this.getClosestBoxToGoal(boxes, goal);
            if (box != null) {
                int box2goalDistance = this.measurer.distance(box.getPosition(), goal.getPosition());
                int agent2boxDistance = this.measurer.distance(agent.getPosition(), box.getPosition());
                int walkDistance = agent2boxDistance + box2goalDistance;
                sum += box2goalDistance;
                if (walkDistance < minWalkDistance) {
                    minAgent2BoxDistance = agent2boxDistance;
                    minWalkDistance = walkDistance;
                }
            }
        }
        if (minAgent2BoxDistance == Integer.MAX_VALUE) {
            return sum + nGoals * this.measurer.getV();
        } else {
            return minAgent2BoxDistance + sum + nGoals * this.measurer.getV();
        }
    }

    class Measurer {
        private int V;
        private HashMap<PositionPair, Integer> D;
        private HashMap<Position, Integer> position2vertex;
        private ArrayList<Position> vertex2position;

        public Measurer(State state) {
            // count number of non-wall objects
            // and create vertex maps
            this.D = new HashMap<>();
            this.position2vertex = new HashMap<>();
            this.vertex2position = new ArrayList<>();
            this.V = 0;
            for (int i = 0; i < state.getWalls().length; i++) {
                boolean[] row = state.getWalls()[i];
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

        private int breadthFirstSearch(Position start, Position end) {
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
            return Integer.MAX_VALUE;
        }

        public int distance(Position p1, Position p2) {
            PositionPair pair = new PositionPair(p1, p2);
            int distance = this.D.getOrDefault(pair, -1);
            if (distance == -1) {
                distance = this.breadthFirstSearch(p1, p2);
                this.D.put(pair, distance);
            }
            return distance;
        }

        public int getV() {
            return this.V;
        }
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

    class BFSNode {
        public int distance;
        public Position position;

        public BFSNode(int distance, Position position) {
            this.distance = distance;
            this.position = position;
        }
    }
}
