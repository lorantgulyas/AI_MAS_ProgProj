package client.heuristics;

import client.NotImplementedException;
import client.state.*;
import client.definitions.AHeuristic;

import java.util.ArrayDeque;
import java.util.Arrays;

public class Floodfill extends AHeuristic {
    private int[][][][] matrix;
    private boolean[][] rooms;
    public static final int UNDISCOVERED = -1;

    public Floodfill(State initialState) {
        super(initialState);
        int rowCount = State.getRowCount();
        int colCount = State.getColCount();

        // matrix starts with goal coordinates, in order to make the memory layout nicer
        matrix = new int[colCount][rowCount][colCount][rowCount];
        for (int goalX = 0; goalX < colCount; goalX++) {
            for (int goalY = 0; goalY < rowCount; goalY++) {
                fillSubMatrix(goalX, goalY, initialState);
            }
        }
    }

    private void fillSubMatrix(int goalX, int goalY, State initialState) {
        boolean[][] walls = initialState.getWalls();

        // preemptively fill all values with -1
        for (int[] rowTo : matrix[goalX][goalY])
            Arrays.fill(rowTo, UNDISCOVERED);

        // if goal position is not wall
        if (!walls[goalX][goalY]) {
            int currentDistance = 0;
            matrix[goalX][goalY][goalX][goalY] = currentDistance;
            ArrayDeque<Position> toExplore = new ArrayDeque<>();
            Position pos = new Position(goalX, goalY);
            toExplore.add(pos);
            while (!toExplore.isEmpty()) {
                pos = toExplore.pop();
                int x = pos.getCol(),  y = pos.getRow();
                currentDistance = matrix[goalX][goalY][x][y] + 1;

                // ugly shit
                if (!walls[x][y - 1] && matrix[goalX][goalY][x][y - 1] == UNDISCOVERED) {
                    matrix[goalX][goalY][x][y - 1] = currentDistance;
                    toExplore.add(new Position(x, y - 1));
                }
                if (!walls[x][y + 1] && matrix[goalX][goalY][x][y + 1] == UNDISCOVERED) {
                    matrix[goalX][goalY][x][y + 1] = currentDistance;
                    toExplore.add(new Position(x, y + 1));
                }
                if (!walls[x - 1][y] && matrix[goalX][goalY][x - 1][y] == UNDISCOVERED) {
                    matrix[goalX][goalY][x - 1][y] = currentDistance;
                    toExplore.add(new Position(x - 1, y));
                }
                if (!walls[x + 1][y] && matrix[goalX][goalY][x + 1][y] == UNDISCOVERED) {
                    matrix[goalX][goalY][x + 1][y] = currentDistance;
                    toExplore.add(new Position(x + 1, y));
                }
            }
        }
    }

    public void findRooms() {
        // return if already preprocessed
        if (rooms != null) return;

        boolean[][] walls = State.getWalls();
        // TODO: add goal positions!!!
        // TODO: fallback on storages, when there are no rooms (half plus on map)
        rooms = new boolean[State.getColCount()][State.getRowCount()];
        // prefill
        for (int y = 0; y < State.getRowCount(); y++)
            for (int x = 0; x < State.getColCount(); x++)
                rooms[x][y] = false;
        // detect rooms
        for (int y = 1; y < State.getRowCount() - 2; y++) {
            for (int x = 1; x < State.getColCount() - 2; x++) {
                // 2x2 window
                if (!(walls[x][y] || walls[x + 1][y] || walls[x][y + 1] || walls[x + 1][y + 1])) {
                    rooms[x][y] = rooms[x + 1][y] = rooms[x][y + 1] = rooms[x + 1][y + 1] = true;
                }
            }
        }

        // print
        StringBuilder s = new StringBuilder();
        for (int y = 0; y < State.getRowCount(); y++) {
            for (int x = 0; x < State.getColCount(); x++) {
                if (rooms[x][y]) {
                    s.append("O");
                } else if (walls[x][y]) {
                    s.append("X");
                } else {
                    s.append(" ");
                }
            }
            s.append("\n");
        }
        System.err.println(s.toString());
    }

    public void prioritizeGoals() {
        for (Goal goal : State.getGoals()) {
            Position pos = goal.getPosition();
            int priority = Integer.MAX_VALUE;
            for (int y = 0; y < State.getRowCount(); y++) {
                for (int x = 0; x < State.getColCount(); x++) {
                    if (rooms[x][y] && (matrix[pos.getCol()][pos.getRow()][x][y] < priority)) {
                        priority = matrix[pos.getCol()][pos.getRow()][x][y];
                    }
                }
            }
            if (priority != Integer.MAX_VALUE) {
                goal.setPriority(priority);
            }
        }

        // print
        for (Goal goal : State.getGoals()) {
            System.err.println(goal);
        }
    }

    public void printMatrix(int goalX, int goalY) {
        StringBuilder s = new StringBuilder();

        for (int y = 0; y < State.getRowCount(); y++) {
            for (int x = 0; x < State.getColCount(); x++) {
                if (matrix[goalX][goalY][x][y] == -1) {
                    s.append("///");
                } else {
                    s.append(String.format("%3s", matrix[goalX][goalY][x][y]));
                }
            }
            s.append("\n");
        }
        System.err.println(s.toString());
    }

    public int distance(int fromX, int fromY, int toX, int toY) {
        return matrix[toX][toY][fromX][fromY];
    }

    public int distance(Position from, Position to) {
        return matrix[to.getCol()][to.getRow()][from.getCol()][from.getRow()];
    }

    @Override
    public int h(State state) {
        int h = 0;
        Agent[] agents = state.getAgents();
        Box[] boxes = state.getBoxes();
        Goal[] goals = State.getGoals();
        for (Goal goal : goals) {
            for (Agent agent : agents) {
                h += this.distance(goal.getPosition(), agent.getPosition());
            }
        }
        for (Box box : boxes) {
            for (Agent agent : agents) {
                h += this.distance(box.getPosition(), agent.getPosition());
            }
        }
        return h;
    }

}
