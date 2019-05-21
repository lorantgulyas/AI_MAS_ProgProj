package client.state;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class SubState {

    private int[] agentIDMap;
    private State state;

    /**
     * Creates a substate for a closed room in the main state.
     * @param mainState State that the substate should be derived for.
     * @param room Room inside of the main state that defines the substate.
     */
    public SubState(State mainState, HashSet<Position> room) {
        // find borders of room
        int minRow = this.getMinRow(room);
        int minCol = this.getMinCol(room);
        int maxRow = this.getMaxRow(room);
        int maxCol = this.getMaxCol(room);
        // plus 3: 1 for actual length and 2 for adding a wall around the level
        int rows = maxRow - minRow + 3;
        int cols = maxCol - minCol + 3;

        // construct substate
        Level mainLevel = mainState.getLevel();
        boolean[][] walls = this.initializeWalls(cols, rows);
        ArrayList<Integer> agentIDList = new ArrayList<>();
        ArrayList<Agent> agentsList = new ArrayList<>();
        ArrayList<Box> boxesList = new ArrayList<>();
        ArrayList<Goal> goalsList = new ArrayList<>();
        int agentID = 0;
        int boxID = 0;
        for (int x = 1; x < cols - 1; x++) {
            int col = minCol + x - 1;
            for (int y = 1; y < rows - 1; y++) {
                int row = minRow + y - 1;
                Position mainPosition = new Position(col, row);
                boolean contained = room.contains(mainPosition);
                walls[x][y] = !contained || mainLevel.wallAt(mainPosition);
                if (contained) {
                    Position subPosition = new Position(x, y);
                    if (mainLevel.goalAt(mainPosition)) {
                        Goal mainGoal = mainLevel.getGoalAt(mainPosition);
                        Goal subGoal = new Goal(mainGoal.getLetter(), subPosition, mainGoal.getColor());
                        goalsList.add(subGoal);
                    }
                    if (mainState.agentAt(mainPosition)) {
                        Agent mainAgent = mainState.getAgentAt(mainPosition);
                        Agent subAgent = new Agent(agentID, mainAgent.getColor(), subPosition);
                        agentIDList.add(mainAgent.getId());
                        agentID++;
                        agentsList.add(subAgent);
                    }
                    if (mainState.boxAt(mainPosition)) {
                        Box mainBox = mainState.getBoxAt(mainPosition);
                        Box subBox = new Box(boxID, mainBox.getLetter(), mainBox.getColor(), subPosition);
                        boxID++;
                        boxesList.add(subBox);
                    }
                }
            }
        }

        // set variables
        this.agentIDMap = this.getAgentIDMap(agentIDList);
        Agent[] agents = agentsList.toArray(new Agent[0]);
        Box[] boxes = boxesList.toArray(new Box[0]);
        Goal[] goals = goalsList.toArray(new Goal[0]);
        Level level = new Level(walls, rows, cols, goals);

        // set agents goals
        Goal[][] agentsGoals = new Goal[agents.length][];
        for (Agent agent : agents) {
            ArrayList<Goal> agentGoals = new ArrayList<>();
            for (Goal goal : goals) {
                if (goal.getColor() == agent.getColor()) {
                    agentGoals.add(goal);
                }
            }
            agentsGoals[agent.getId()] = agentGoals.toArray(new Goal[0]);
        }
        level.setAgentsGoals(agentsGoals);

        this.state = new State(level, agents, boxes);
    }

    /**
     * @return The substate.
     */
    public State getState() {
        return state;
    }

    public int[] getAgentIDMap() {
        return this.agentIDMap;
    }

    /**
     * Maps agent IDs in the substate to their ID in the main state.
     * @param id ID of the agent in the substate.
     * @return ID of the agent in the main state.
     */
    public int getOriginalAgentID(int id) {
        return this.agentIDMap[id];
    }

    private int getMinRow(HashSet<Position> room) {
        int minRow = Integer.MAX_VALUE;
        for (Position position : room) {
            minRow = Math.min(minRow, position.getRow());
        }
        return minRow;
    }

    private int getMinCol(HashSet<Position> room) {
        int minCol = Integer.MAX_VALUE;
        for (Position position : room) {
            minCol = Math.min(minCol, position.getCol());
        }
        return minCol;
    }

    private int getMaxRow(HashSet<Position> room) {
        int maxRow = Integer.MIN_VALUE;
        for (Position position : room) {
            maxRow = Math.max(maxRow, position.getRow());
        }
        return maxRow;
    }

    private int getMaxCol(HashSet<Position> room) {
        int maxCol = Integer.MIN_VALUE;
        for (Position position : room) {
            maxCol = Math.max(maxCol, position.getCol());
        }
        return maxCol;
    }

    private boolean[][] initializeWalls(int cols, int rows) {
        boolean[][] walls = new boolean[cols][rows];
        for (int x = 0; x < cols; x++) {
            walls[x][0] = true;
            walls[x][rows - 1] = true;
        }
        for (int y = 0; y < rows; y++) {
            walls[0][y] = true;
            walls[cols - 1][y] = true;
        }
        return walls;
    }

    private int[] getAgentIDMap(ArrayList<Integer> agentIDList) {
        int nAgents = agentIDList.size();
        int[] agentIDMap = new int[nAgents];
        for (int i = 0; i < nAgents; i++) {
            agentIDMap[i] = agentIDList.get(i);
        }
        return agentIDMap;
    }

}
