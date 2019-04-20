package client.state;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Level {
    private boolean[][] walls;
    private int rowCount;
    private int colCount;
    private Goal[] goals;
    private HashMap<Position, Goal> goalMap;

    public Level(boolean[][] walls, int rowCount, int colCount, Goal[] goals) {
        this.walls = walls;
        this.rowCount = rowCount;
        this.colCount = colCount;
        this.goals = goals;
        this.goalMap = new HashMap<>();
        for (Goal goal : goals) {
            this.goalMap.put(goal.getPosition(), goal);
        }
    }

    public boolean[][] getWalls() {
        return this.walls;
    }

    public int getColCount() {
        return this.colCount;
    }

    public int getRowCount() {
        return this.rowCount;
    }

    public Goal[] getGoals() {
        return goals;
    }

    public boolean goalAt(Position position) {
        return this.goalMap.containsKey(position);
    }

    public Goal getGoalAt(Position position) {
        return this.goalMap.get(position);
    }

    public boolean wallAt(Position position) {
        return this.walls[position.getCol()][position.getRow()];
    }

}