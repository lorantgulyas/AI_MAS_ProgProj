package client;

import client.definitions.AState;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class State extends AState {
    private static boolean[][] walls;
    private static int ROW_COUNT;
    private static int COL_COUNT;
    private Agent[] agents;
    private Box[] boxes;
    private Goal[] goals;
    private HashMap<Position, Agent> agentMap;
    private HashMap<Position, Box> boxMap;

    public State(Agent[] agents, Box[] boxes, Goal[] goals) {
        this.agents = agents;
        this.boxes = boxes;
        this.goals = goals;
        agentMap = new HashMap<>();
        for (Agent agent : agents) {
            agentMap.put(agent.getPosition(), agent);
        }
        boxMap = new HashMap<>();
        for (Box box : boxes) {
            boxMap.put(box.getPosition(), box);
        }
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
        return !walls[position.getCol()][position.getRow()] &&
                !agentMap.containsKey(position) &&
                !boxMap.containsKey(position);
    }

    public Agent[] getAgents() {
        return agents;
    }

    public Box[] getBoxes() {
        return boxes;
    }

    public Goal[] getGoals() {
        return goals;
    }

    public boolean agentIsDone(int agentID) {
        Agent agent = this.agents[agentID];
        ArrayList<Goal> agentGoals = new ArrayList<>();
        for (Goal goal : this.goals) {
            for (Box box : this.boxes) {
                if (box.getLetter() == goal.getLetter() && box.getColor() == agent.getColor()) {
                    agentGoals.add(goal);
                }
            }
        }
        for (Goal goal : agentGoals) {
            Box box = boxMap.getOrDefault(goal.getPosition(), null);
            if (box == null || box.getLetter() != goal.getLetter()) {
                return false;
            }
        }
        return true;
    }

    public boolean isGoalState() {
        for (Goal goal : goals) {
            Box box = boxMap.getOrDefault(goal.getPosition(), null);
            if (box == null || box.getLetter() != goal.getLetter()) {
                return false;
            }
        }
        return true;
    }

    public Box getBoxAt(Position position) {
        return this.boxMap.get(position);
    }

    public boolean boxAt(Position position) {
        return this.boxMap.containsKey(position);
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

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj.getClass() != this.getClass()) {
            return false;
        }
        State state = (State) obj;
        return Arrays.deepEquals(this.agents, state.agents) && Arrays.deepEquals(this.boxes, state.boxes);
    }

    @Override
    public int hashCode() {
        // TODO: change this!
        return 10;
    }
}