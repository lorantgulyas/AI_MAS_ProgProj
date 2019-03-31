package client;

import client.definitions.AState;

public class State extends AState {
    private static boolean[][] walls;
    private static int ROW_COUNT;
    private static int COL_COUNT;
    private Agent[] agents;
    private Box[] boxes;
    private Goal[] goals;

    public State(Agent[] agents, Box[] boxes, Goal[] goals) {
        this.agents = agents;
        this.boxes = boxes;
        this.goals = goals;
    }

    public static void setLevel(boolean[][] initialWalls,
                                int rowCount, int colCount) {
        walls = initialWalls;
        ROW_COUNT = rowCount;
        COL_COUNT = colCount;
    }

    public boolean[][] getWalls() {
        return walls;
    }

    public static int getColCount() {
        return COL_COUNT;
    }

    public static int getRowCount() {
        return ROW_COUNT;
    }

    public boolean isFree(Position position) {
        throw new NotImplementedException();
    }

    public Agent[] getAgents() {
        throw new NotImplementedException();
    }

    public Box[] getBoxes() {
        throw new NotImplementedException();
    }

    public Goal[] getGoals() {
        throw new NotImplementedException();
    }

    public Command[] extractPlan() {
        throw new NotImplementedException();
    }

    public boolean isGoalState() {
        throw new NotImplementedException();
    }

    public AState[] getExpandedStates() {
        throw new NotImplementedException();
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        char[][] level = new char[COL_COUNT][ROW_COUNT];
        for (int y = 0; y < ROW_COUNT; y++) {
            for (int x = 0; x < COL_COUNT; x++) {
                if (walls[x][y]) {
                    level[x][y] = '+';
                } else {
                    level[x][y] = ' ';
                }
            }
        }
        for (Goal goal : goals) {
            Position pos = goal.getPosition();
            level[pos.getCol()][pos.getRow()] = Character.toLowerCase(goal.getLetter());
            s.append("goal at: ");
            s.append(goal.toString());
            s.append("\n");
        }
        for (Box box : boxes) {
            Position pos = box.getPosition();
            level[pos.getCol()][pos.getRow()] = box.getLetter();
        }
        for (Agent agent : agents) {
            Position pos = agent.getPosition();
            level[pos.getCol()][pos.getRow()] = Character.forDigit(agent.getId(), 10);
        }
        for (int y = 0; y < ROW_COUNT; y++) {
            for (int x = 0; x < COL_COUNT; x++) {
                s.append(level[x][y]);
            }
            s.append("\n");
        }
        return s.toString();
    }
}