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
    private Goal[][] agentsGoals;

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

    /**
     * Usage: agent0Goals = level.getAgentGoals(0)
     *
     * @param agentID ID of the agent to get goals for.
     * @return Goals that the given agent can help achieve.
     */
    public Goal[] getAgentGoals(int agentID) {
        return this.agentsGoals[agentID];
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

    public void setAgentsGoals(Goal[][] agentsGoals) {
        this.agentsGoals = agentsGoals ;
    }

    public boolean wallAt(Position position) {
        return this.walls[position.getCol()][position.getRow()];
    }

}