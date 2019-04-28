package client.state;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class State {
    private Agent[] agents;
    private Box[] boxes;
    private Level level;
    private HashMap<Position, Agent> agentMap;
    private HashMap<Position, Box> boxMap;
    private int h;
    private int _hash;

    /**
     * Constructs a new state.
     *
     * @param level Associated level (walls and goals).
     * @param agents Agents in the state.
     * @param boxes Boxes in the state.
     */
    public State(Level level, Agent[] agents, Box[] boxes) {
        this.agents = agents;
        this.boxes = boxes;
        this.level = level;
        this.agentMap = new HashMap<>();
        for (Agent agent : agents) {
            this.agentMap.put(agent.getPosition(), agent);
        }
        this.boxMap = new HashMap<>();
        for (Box box : boxes) {
            this.boxMap.put(box.getPosition(), box);
        }
        this._hash = this.computeHashCode();
    }

    public Level getLevel() {
        return level;
    }

    public boolean isFree(Position position) {
        return !this.level.wallAt(position) &&
                !this.agentMap.containsKey(position) &&
                !this.boxMap.containsKey(position);
    }

    public Agent[] getAgents() {
        return agents;
    }

    public Box[] getBoxes() {
        return boxes;
    }

    /**
     * Retrieves heuristic value.
     * @return
     */
    public int h() {
        return this.h;
    }

    /**
     * Sets heuristic value. This is implemented as a setter rather than as a constructor
     * since the heuristic functon needs to compute h on a state. Therefore we need to
     * construct the state, compute h and then associate h with the state.
     *
     * @param h Heuristic value.
     */
    public void setH(int h) {
        this.h = h;
    }

    // TODO: improve this implementation
    public boolean agentIsDone(int agentID) {
        Agent agent = this.agents[agentID];
        ArrayList<Goal> agentGoals = new ArrayList<>();
        for (Goal goal : this.level.getGoals()) {
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
        //for (Goal goal : this.level.getGoals()) {
        //    Box box = this.boxMap.getOrDefault(goal.getPosition(), null);
        //    if (box == null || box.getLetter() != goal.getLetter()) {
        //        return false;
        //    }
        //}
        //return true;
        // TODO: uncomment above and remove below
        for (Agent agent : this.agents) {
            if (!this.agentIsDone(agent.getId())) {
                return false;
            }
        }
        return true;
    }

    public Agent getAgentAt(Position position) {
        return this.agentMap.get(position);
    }

    public boolean agentAt(Position position) {
        return this.agentMap.containsKey(position);
    }

    public Box getBoxAt(Position position) {
        return this.boxMap.get(position);
    }

    public boolean boxAt(Position position) {
        return this.boxMap.containsKey(position);
    }

    @Override
    public String toString() {
        boolean[][] walls = this.level.getWalls();
        int rowCount = this.level.getRowCount();
        int colCount = this.level.getColCount();
        Goal[] goals = this.level.getGoals();
        StringBuilder s = new StringBuilder();
        char[][] level = new char[colCount][rowCount];
        for (int y = 0; y < rowCount; y++) {
            for (int x = 0; x < colCount; x++) {
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
        for (int y = 0; y < rowCount; y++) {
            for (int x = 0; x < colCount; x++) {
                s.append(level[x][y]);
            }
            s.append("\n");
        }
        return s.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (obj.getClass() != this.getClass())
            return false;
        State state = (State) obj;
        return Arrays.deepEquals(this.agents, state.agents)
                && Arrays.deepEquals(this.boxes, state.boxes);
    }

    @Override
    public int hashCode() {
        return this._hash;
    }

    private int computeHashCode() {
        int a = 0;
        for (Agent agent : this.agents) {
            a += agent.hashCode();
        }
        int b = 0;
        for (Box box : this.boxes) {
            b += box.hashCode();
        }
        return a * b;
    }
}