package client.heuristics;

import client.*;
import client.definitions.AHeuristic;

import java.util.ArrayList;
import java.util.HashMap;

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
        // 500^3 integers is approximately 100 MB
        private final int THRESHOLD = 500;

        private int V;
        private int[][] D;
        private HashMap<Position, Integer> position2vertex;
        private ArrayList<Position> vertex2position;

        public Measurer(State state) {
            // count number of non-wall objects
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

            System.err.println("V = " + this.V);

            if (this.V < this.THRESHOLD) {
                this.initializeFloydWarshall();
            }
        }

        private void initializeFloydWarshall() {
            // initialize vertex distances
            this.D = new int[this.V][this.V];
            int infinity = Integer.MAX_VALUE / this.V;
            for (int i = 0; i < this.V; i++) {
                Position p1 = this.vertex2position.get(i);
                int p1row = p1.getRow();
                int p1col = p1.getCol();
                for (int j = 0; j < this.V; j++) {
                    Position p2 = this.vertex2position.get(j);
                    int p2row = p2.getRow();
                    int p2col = p2.getCol();
                    if (i == j) {
                        this.D[i][j] = 0;
                    } else if ((p1col == p2col && (p1row == p2row - 1 || p1row == p2row + 1))
                            || (p1row == p2row && (p1col == p2col - 1 || p1col == p2col + 1))) {
                        this.D[i][j] = 1;
                    } else {
                        this.D[i][j] = infinity;
                    }
                }
            }

            // run floyd-warshall
            for (int k = 0; k < this.V; k++) {
                for (int i = 0; i < this.V; i++) {
                    for (int j = 0; j < this.V; j++) {
                        int d = this.D[i][k] + this.D[k][j];
                        if (this.D[i][j] > d) {
                            this.D[i][j] = d;
                        }
                    }
                }
            }
        }

        public int distance(Position p1, Position p2) {
            if (this.V < this.THRESHOLD) {
                int v1 = this.position2vertex.get(p1);
                int v2 = this.position2vertex.get(p2);
                return this.D[v1][v2];
            } else {
                return this.manhattan(p1, p2);
            }
        }

        public int getV() {
            return this.V;
        }

        private int manhattan(Position p1, Position p2) {
            return Math.abs(p1.getRow() - p2.getRow()) + Math.abs(p1.getCol() - p2.getCol());
        }
    }
}
