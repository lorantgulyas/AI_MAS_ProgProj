package client.heuristics;

import client.Position;
import client.State;
import client.definitions.AHeuristic;
import client.definitions.AState;

import java.util.ArrayDeque;
import java.util.Arrays;

public class Floodfill extends AHeuristic {
    private int[][][][] matrix;
    public static final int UNDISCOVERED = -1;

    public Floodfill(State initialState) {
        super(initialState);
        boolean[][] walls = initialState.getWalls();
        int rowCount = State.getRowCount();
        int colCount = State.getColCount();

        int goalX = 2;
        int goalY = 1;

        // matrix starts with goal coordinates, in order to make the memory layout nicer
        matrix = new int[colCount][rowCount][colCount][rowCount];
        // for loop here
        {
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

        printDistanceMatrix(goalX, goalY);
    }

    public void printDistanceMatrix(int goalX, int goalY) {
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

    public int getDistance(int fromX, int fromY, int toX, int toY) {
        return matrix[toX][toY][fromX][fromY];
    }

    @Override
    public int h(AState state) {
        return 0;
    }

}
