package client.state;

import java.util.HashMap;

public class Level {
    private boolean[][] walls;
    private int rowCount;
    private int colCount;
    private Goal[] goals;
    private HashMap<Position, Goal> goalMap;
    private Goal[][] agentsGoals;
    private HashMap<Position, Integer> goalIndexMap;

    public Level(boolean[][] walls, int rowCount, int colCount, Goal[] goals) {
        this.walls = walls;
        this.rowCount = rowCount;
        this.colCount = colCount;
        this.setGoals(goals);
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
        return this.goals;
    }

    public HashMap<Position, Integer> getGoalIndexMap() {
        return this.goalIndexMap;
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

    /**
     * WARNING: Do not use this method in a multi-agent setting!
     * @param goals
     */
    public void setGoals(Goal[] goals) {
        this.goals = goals;
        this.goalMap = new HashMap<>();
        for (Goal goal : goals) {
            this.goalMap.put(goal.getPosition(), goal);
        }
        this.goalIndexMap = new HashMap<>();
        for (int i = 0; i < goals.length; i++) {
            this.goalIndexMap.put(goals[i].getPosition(), i);
        }
    }

}