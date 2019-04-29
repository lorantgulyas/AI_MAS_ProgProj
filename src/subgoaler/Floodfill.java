package subgoaler;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;

public class Floodfill implements Comparator<State> {
    private int[][][][] matrix;
    private boolean[][] rooms;
    public static final int UNDISCOVERED = -1;

    public Floodfill(State initialState) {
        int xMax = State.getXmax();
        int yMax = State.getYmax();

        // matrix starts with goal coordinates, in order to make the memory layout nicer
        matrix = new int[yMax][xMax][yMax][xMax];
        for (int goalY = 0; goalY < yMax; goalY++) {
            for (int goalX = 0; goalX < xMax; goalX++) {
                fillSubMatrix(goalY, goalX);
            }
        }
    }

    private void fillSubMatrix(int goalY, int goalX) {
        boolean[][] walls = State.getWalls();

        // preemptively fill all values with -1
        for (int[] rowTo : matrix[goalY][goalX])
            Arrays.fill(rowTo, UNDISCOVERED);

        // if goal position is not wall
        if (!walls[goalY][goalX]) {
            int currentDistance = 0;
            matrix[goalY][goalX][goalY][goalX] = currentDistance;
            ArrayDeque<Position> toExplore = new ArrayDeque<>();
            Position pos = new Position(goalX, goalY);
            toExplore.add(pos);
            while (!toExplore.isEmpty()) {
                pos = toExplore.pop();
                int x = pos.getX(),  y = pos.getY();
                currentDistance = matrix[goalY][goalX][y][x] + 1;

                // ugly shit
                if (!walls[y - 1][x] && matrix[goalY][goalX][y - 1][x] == UNDISCOVERED) {
                    matrix[goalY][goalX][y - 1][x] = currentDistance;
                    toExplore.add(new Position(x, y - 1));
                }
                if (!walls[y + 1][x] && matrix[goalY][goalX][y + 1][x] == UNDISCOVERED) {
                    matrix[goalY][goalX][y + 1][x] = currentDistance;
                    toExplore.add(new Position(x, y + 1));
                }
                if (!walls[y][x - 1] && matrix[goalY][goalX][y][x - 1] == UNDISCOVERED) {
                    matrix[goalY][goalX][y][x - 1] = currentDistance;
                    toExplore.add(new Position(x - 1, y));
                }
                if (!walls[y][x + 1] && matrix[goalY][goalX][y][x + 1] == UNDISCOVERED) {
                    matrix[goalY][goalX][y][x + 1] = currentDistance;
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
        rooms = new boolean[State.getYmax()][State.getXmax()];
        // prefill
        for (int y = 0; y < State.getYmax(); y++)
            for (int x = 0; x < State.getXmax(); x++)
                rooms[y][x] = false;
        // detect rooms
        for (int y = 1; y < State.getYmax() - 2; y++) {
            for (int x = 1; x < State.getXmax() - 2; x++) {
                // 2x2 window
                if (!(walls[y][x] || walls[y][x + 1] || walls[y + 1][x] || walls[y + 1][x + 1])) {
                    rooms[y][x] = rooms[y][x + 1] = rooms[y + 1][x] = rooms[y + 1][x + 1] = true;
                }
            }
        }

        // print
        StringBuilder s = new StringBuilder();
        for (int y = 0; y < State.getYmax(); y++) {
            for (int x = 0; x < State.getXmax(); x++) {
                if (rooms[y][x]) {
                    s.append("O");
                } else if (walls[y][x]) {
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
            for (int y = 0; y < State.getYmax(); y++) {
                for (int x = 0; x < State.getXmax(); x++) {
                    if (rooms[y][x] && (matrix[pos.getY()][pos.getX()][y][x] < priority)) {
                        priority = matrix[pos.getY()][pos.getX()][y][x];
                    }
                }
            }
            if (priority != Integer.MAX_VALUE) {
                goal.setPriority(priority);
            }
        }

        Arrays.sort(State.getGoals(), Comparator.comparingInt(Goal::getPriority).reversed());

        // print
        for (Goal goal : State.getGoals()) {
            System.err.println(goal);
        }
    }

    public void printMatrix(int goalX, int goalY) {
        StringBuilder s = new StringBuilder();

        for (int y = 0; y < State.getYmax(); y++) {
            for (int x = 0; x < State.getXmax(); x++) {
                if (matrix[goalY][goalX][y][x] == -1) {
                    s.append("///");
                } else {
                    s.append(String.format("%3s", matrix[goalY][goalX][y][x]));
                }
            }
            s.append("\n");
        }
        System.err.println(s.toString());
    }

    public int distance(int fromX, int fromY, int toX, int toY) {
        return matrix[toY][toX][fromY][fromX];
    }

    public int distance(Position from, Position to) {
        return matrix[to.getY()][to.getX()][from.getY()][from.getX()];
    }

    @Override
    public int compare(State n1, State n2) {
        return n1.g() + h(n1) - n2.g() - h(n2);
    }

    public int h(State state) {
        int h = 0;
        Agent[] agents = state.getAgents();
        Goal[] goals = State.getGoals();
        HashSet<Box> assignedBoxes = new HashSet<>();
        Box[] boxes = state.getBoxes();

        Box minBox = boxes[0];
        for (Goal goal : goals) {
            int minDistance = Integer.MAX_VALUE;
            for (Box box : boxes) {
                if (!assignedBoxes.contains(box) &&
                        (minDistance > distance(box.getPosition(), goal.getPosition())) &&
                        (box.getLetter() == goal.getLetter())) {
                    minBox = box;
                    minDistance = distance(box.getPosition(), goal.getPosition());
                }
            }
            assignedBoxes.add(minBox);
            h += minDistance;
        }

        h += distance(minBox.getPosition(), agents[0].getPosition());
        return h;
    }
}
