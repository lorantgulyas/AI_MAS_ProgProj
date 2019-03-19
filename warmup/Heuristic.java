package searchclient;

import java.util.Comparator;
import java.awt.Point;
import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Arrays;

public abstract class Heuristic implements Comparator<State> {

    private ArrayList<Point> goalLocations = new ArrayList<Point>();
    private ArrayList<Point> boxLocations = new ArrayList<Point>();
    // copy the level, fill each cell with an integer corresponding to the distance to a specific goal
    // store these distance matrices for each goal on the map
    private int distanceMatrices[][][];
    private static final int UNDISCOVERED = -1;

    public Heuristic(State initialState) {
        // Here's a chance to pre-process the static parts of the level.
        // collect all the goals from the level and add them to an ArrayList
        for (int row = 1; row < State.MAX_ROW - 1; row++) {
            for (int col = 1; col < State.MAX_COL - 1; col++) {
                if (State.goals[row][col] > 0) {
                    goalLocations.add(new Point(row,col));
                }
                if (initialState.boxAt(row,col)) {
                    boxLocations.add(new Point(row,col));
                }
            }
        }
        // define the distanceMatrices' correct size, now that the number of goals is known
        distanceMatrices = new int[goalLocations.size()][State.MAX_ROW][State.MAX_COL];
        // and create the distanceMatrix for each goal
        for (int goalIndex = 0; goalIndex<goalLocations.size(); goalIndex++) {
            distanceMatrices[goalIndex] = createDistanceMatrix(goalLocations.get(goalIndex));
        }
    }

    private int[][] createDistanceMatrix(Point origin){
        // create the distanceMatrix, fill it with UNDISCOVERED values, initialize distance to 0 and set the origin point's value to 0
        int currentDistance = 0;
        int [][] distanceMatrix = new int[State.MAX_ROW][State.MAX_COL];
        for (int[] row: distanceMatrix) {
            Arrays.fill(row, UNDISCOVERED);
        }
        distanceMatrix[origin.x][origin.y] = currentDistance;
        currentDistance++;
        // call expand function which returns 0,1,2,3 or 4 Points depending on how many visitable cells are around the origin cell
        // queue cannot be used since distance has to be increased after every expand call, not after every cell
        ArrayList<Point> cellsToExplore = expand(distanceMatrix, currentDistance, origin);
        // iterate untill there are visitable cells left and after, return the distanceMatrix
        while (!cellsToExplore.isEmpty()){
            currentDistance++;
            ArrayList<Point> newCells = new ArrayList<>();
            for (int index = 0; index < cellsToExplore.size(); index++){
                newCells.addAll(expand(distanceMatrix,currentDistance,cellsToExplore.get(index)));
            }
            cellsToExplore = newCells;
        } 
        return distanceMatrix;
    }

    private ArrayList<Point> expand(int[][] distanceMatrix, int currentDistance, Point subOrigin){
        int x = subOrigin.x;
        int y = subOrigin.y;
        ArrayList<Point> discoveryQueue = new ArrayList<>();
        ArrayList<Point> potentialDirections = new ArrayList<>();
        potentialDirections.add(new Point(x-1,y));
        potentialDirections.add(new Point(x+1,y));
        potentialDirections.add(new Point(x,y-1));
        potentialDirections.add(new Point(x,y+1));
        for (Point pd: potentialDirections){
            if (validCell(pd.x,pd.y)) {
                if (distanceMatrix[pd.x][pd.y] == UNDISCOVERED) {
                    distanceMatrix[pd.x][pd.y] = currentDistance;
                    discoveryQueue.add(pd);
                }
            }
        }
        return discoveryQueue;
    }

    // checks if cell can be accessed by the agent or not
    private boolean validCell(int row, int col) {
        if(row < State.MAX_ROW && col < State.MAX_COL){
            if(row > 0 && col > 0){
                if(!State.walls[row][col]) {
                    return true;
                }
            }
        }
        return false;
    }

    public int h(State n) {
        int heuristic = 0;
        // look for boxes throughout the map
        for(int row = 0; row < State.MAX_ROW; row++) {
            for (int col = 0; col < State.MAX_COL; col++) {
                if (n.boxAt(row,col)) { //is there a box to examine
                    char boxGoal = Character.toLowerCase(n.boxes[row][col]);
                    int closestGoalDistance = Integer.MAX_VALUE;
                    int distanceToAgent = 0;
                    if (n.goals[row][col] != boxGoal) { // is the box already in a goal position
                        for (int goalIndex = 0; goalIndex<goalLocations.size(); goalIndex++) {
                            // find the closest suitable goal to the box
                            Point currentGoal = goalLocations.get(goalIndex);
                            if (State.goals[currentGoal.x][currentGoal.y] == boxGoal && // is the goal's code the same as the box's code
                                n.boxes[currentGoal.x][currentGoal.y] != n.boxes[row][col]) { // is there a box already with the same code
                                    int goalDistance = distanceMatrices[goalIndex][row][col];
                                    if (goalDistance < closestGoalDistance) {
                                        closestGoalDistance = goalDistance;
                                    } 
                                }
                        }
                        //only consider boxes still to be moved
                        distanceToAgent = ManhattenDistance(row, col, n.agentRow, n.agentCol);
                    } else {
                        closestGoalDistance = 0;
                    }
                    // the agent spends too much time keeping boxes around, let's give a bigger weight to pushing them towards the closest goal
                    // closestGoalDistance *= goalLocations.size();
                    // closestGoalDistance *= 64;
                    closestGoalDistance *= boxLocations.size();
                    heuristic += closestGoalDistance;
                    heuristic += distanceToAgent;
                }
            }
        }
        return heuristic;
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
            return n.g() + this.h(n);
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
            return this.W  * n.g() + this.h(n);
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
