package client.heuristics;

import client.NotImplementedException;
import client.state.Level;
import client.state.Position;
import client.state.State;
import client.definitions.AHeuristic;

import java.util.ArrayDeque;
import java.util.Arrays;

public class Floodfill extends AHeuristic {
    private int[][][][] matrix;
    public static final int UNDISCOVERED = -1;

    private int rowCount;
    private int colCount;

    public Floodfill(State initialState) {
        super(initialState);
        Level level = initialState.getLevel();
        this.rowCount = level.getRowCount();
        this.colCount = level.getColCount();

        // matrix starts with goal coordinates, in order to make the memory layout nicer
        this.matrix = new int[this.colCount][this.rowCount][this.colCount][this.rowCount];
        for (int goalX = 0; goalX < this.colCount; goalX++) {
            for (int goalY = 0; goalY < this.rowCount; goalY++) {
                fillSubMatrix(goalX, goalY, initialState);
            }
        }

        // for debug purposes
        printMatrix(25, 25);
    }

    private void fillSubMatrix(int goalX, int goalY, State initialState) {
        Level level = initialState.getLevel();
        boolean[][] walls = level.getWalls();

        // preemptively fill all values with -1
        for (int[] rowTo : this.matrix[goalX][goalY])
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
                currentDistance = this.matrix[goalX][goalY][x][y] + 1;

                // ugly shit
                if (!walls[x][y - 1] && this.matrix[goalX][goalY][x][y - 1] == UNDISCOVERED) {
                    this.matrix[goalX][goalY][x][y - 1] = currentDistance;
                    toExplore.add(new Position(x, y - 1));
                }
                if (!walls[x][y + 1] && this.matrix[goalX][goalY][x][y + 1] == UNDISCOVERED) {
                    this.matrix[goalX][goalY][x][y + 1] = currentDistance;
                    toExplore.add(new Position(x, y + 1));
                }
                if (!walls[x - 1][y] && this.matrix[goalX][goalY][x - 1][y] == UNDISCOVERED) {
                    this.matrix[goalX][goalY][x - 1][y] = currentDistance;
                    toExplore.add(new Position(x - 1, y));
                }
                if (!walls[x + 1][y] && this.matrix[goalX][goalY][x + 1][y] == UNDISCOVERED) {
                    this.matrix[goalX][goalY][x + 1][y] = currentDistance;
                    toExplore.add(new Position(x + 1, y));
                }
            }
        }
    }

    public void printMatrix(int goalX, int goalY) {
        StringBuilder s = new StringBuilder();

        for (int y = 0; y < this.rowCount; y++) {
            for (int x = 0; x < this.colCount; x++) {
                if (this.matrix[goalX][goalY][x][y] == -1) {
                    s.append("///");
                } else {
                    s.append(String.format("%3s", this.matrix[goalX][goalY][x][y]));
                }
            }
            s.append("\n");
        }
        System.err.println(s.toString());
    }

    public int distance(int fromX, int fromY, int toX, int toY) {
        return this.matrix[toX][toY][fromX][fromY];
    }

    public int distance(Position from, Position to) {
        return this.matrix[to.getCol()][to.getRow()][from.getCol()][from.getRow()];
    }

    @Override
    public int h(State state) {
        throw new NotImplementedException();
    }

}
