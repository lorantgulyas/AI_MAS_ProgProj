package client;

import client.state.State;

import java.util.Comparator;
import java.awt.Point;
import java.util.ArrayList;

public abstract class Heuristic implements Comparator<State> {

    private ArrayList<Point> goalLocations = new ArrayList<Point>();
    private ArrayList<Point> boxLocations = new ArrayList<Point>();
    // copy the level, fill each cell with an integer corresponding to the distance to a specific goal
    // store these distance matrices for each goal on the map
    private int distanceMatrices[][][];
    private static final int UNDISCOVERED = -1;

    public Heuristic(State initialState) {
        throw new NotImplementedException();
    }

    private int[][] createDistanceMatrix(Point origin){
        throw new NotImplementedException();
    }

    private ArrayList<Point> expand(int[][] distanceMatrix, int currentDistance, Point subOrigin){
        throw new NotImplementedException();
    }

    // checks if cell can be accessed by the agent or not
    private boolean validCell(int row, int col) {
        throw new NotImplementedException();
    }

    public int h(State n) {
        throw new NotImplementedException();
    }

    public abstract int f(State n);

    public int ManhattenDistance(int x1, int y1, int x2, int y2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }

    @Override
    public int compare(State n1, State n2) {
        return this.f(n1) - this.f(n2);
    }

    public static class AStar extends Heuristic {
        public AStar(State initialState) {
            super(initialState);
        }

        @Override
        public int f(State n) {
            throw new NotImplementedException();
        }

        @Override
        public String toString() {
            return "A* evaluation";
        }
    }

    public static class WeightedAStar extends Heuristic {
        private int W;

        public WeightedAStar(State initialState, int W) {
            super(initialState);
            this.W = W;
        }

        @Override
        public int f(State n) {
            throw new NotImplementedException();
        }

        @Override
        public String toString() {
            return String.format("WA*(%d) evaluation", this.W);
        }
    }

    public static class Greedy extends Heuristic {
        public Greedy(State initialState) {
            super(initialState);
        }

        @Override
        public int f(State n) {
            return this.h(n);
        }

        @Override
        public String toString() {
            return "Greedy evaluation";
        }
    }
}
