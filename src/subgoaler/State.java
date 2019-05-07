package subgoaler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

public class State {
    private static final Random RNG = new Random(1);

    private static boolean[][] walls;
    private static int X_MAX;
    private static int Y_MAX;
    private static Goal[] goals;

    private Agent[] agents;
    private Box[] boxes;
    public State parent;
    public Command cmd;
    private int g = 0;

    //private HashMap<Position, Integer> agentMap;
    private HashMap<Position, Integer> boxMap;
    private int _hash = 0;

    public static void setLevel(boolean[][] initWalls, int xMax, int yMax,
                                Goal[] initGoals) {
        walls = initWalls;
        X_MAX = xMax;
        Y_MAX = yMax;
        goals = initGoals;
    }

    public static void setGoals(Goal[] goals) {
        State.goals = goals;
    }

    public State(Agent[] agents, Box[] boxes) {
        this.agents = agents;
//        agentMap = new HashMap<>();
//        for (Agent agent : agents) {
//            agentMap.put(agent.getPosition(), agent);
//        }
        setBoxes(boxes);
    }

    public Agent[] getAgents() {
        return agents;
    }

    public Box[] getBoxes() {
        return boxes;
    }

    public static Goal[] getGoals() {
        return goals;
    }

    public void setBoxes(Box[] boxes) {
        this.boxes = boxes;
        boxMap = new HashMap<>();
        for (int i = 0; i < boxes.length; i++) {
            boxMap.put(boxes[i].getPosition(), i);
        }
    }

    public static boolean[][] getWalls() {
        return walls;
    }

    public static int getXmax() {
        return X_MAX;
    }

    public static int getYmax() {
        return Y_MAX;
    }

    public Command getCommand() {
        return cmd;
    }

    public int g() {
        return g;
    }

    private boolean cellIsFree(Position pos) {
        if (walls[pos.getY()][pos.getX()]) return false;
        if (boxMap.containsKey(pos)) return false;
        // TODO: check agents here
        return true;
    }

    public ArrayList<State> getExpandedStates() {
        // TODO: make agentId dynamic
        int agentId = 0;
        Agent agent = agents[agentId];

        ArrayList<State> expandedStates = new ArrayList<>(Command.EVERY.length);
        for (Command c : Command.EVERY) {
            // Determine applicability of action
            int newAgentX = agent.getPosition().getX() + Command.dirToColChange(c.dir1);
            int newAgentY = agent.getPosition().getY() + Command.dirToRowChange(c.dir1);
            Position newAgentPos = new Position(newAgentX, newAgentY);

            if (c.actionType == Command.Type.Move) {
                // Check if there's a wall or box on the cell to which the agent is moving
                if (this.cellIsFree(newAgentPos)) {
                    State n = ChildState(c);
                    n.agents[0].setPosition(newAgentPos);
                    expandedStates.add(n);
                }
            } else if (c.actionType == Command.Type.Push) {
                // Make sure that there's actually a box to move
                Box oldBox = getBox(newAgentPos);
                if (oldBox != null) {
                    int newBoxX = newAgentX + Command.dirToColChange(c.dir2);
                    int newBoxY = newAgentY + Command.dirToRowChange(c.dir2);
                    Position newBoxPos = new Position(newBoxX, newBoxY);
                    // .. and that new cell of box is free
                    if (this.cellIsFree(newBoxPos)) {
                        State n = ChildState(c);
                        n.agents[0].setPosition(newAgentPos);
                        Integer i = n.boxMap.get(newAgentPos);
                        n.boxMap.remove(newAgentPos);
                        n.boxMap.put(newBoxPos, i);
                        n.boxes[i] = oldBox.copy(newBoxPos);
                        expandedStates.add(n);
                    }
                }
            } else if (c.actionType == Command.Type.Pull) {
                // Cell is free where agent is going
                if (this.cellIsFree(newAgentPos)) {
                    int boxX = agent.getPosition().getX() + Command.dirToColChange(c.dir2);
                    int boxY = agent.getPosition().getY() + Command.dirToRowChange(c.dir2);
                    Position oldBoxPos = new Position(boxX, boxY);
                    // .. and there's a box in "dir2" of the agent
                    Box oldBox = getBox(oldBoxPos);
                    if (oldBox != null) {
                        State n = ChildState(c);
                        n.agents[0].setPosition(newAgentPos);
                        Integer i = n.boxMap.get(oldBoxPos);
                        n.boxMap.remove(oldBoxPos);
                        n.boxMap.put(agent.getPosition(), i);
                        n.boxes[i] = oldBox.copy(agent.getPosition());
                        expandedStates.add(n);
                    }
                }
            }
        }
        Collections.shuffle(expandedStates, RNG);
        return expandedStates;
    }

    private State ChildState(Command cmd) {
        Agent[] newAgents = new Agent[agents.length];
        for (int i = 0; i < agents.length; i++) {
            // TODO: prep for multiagent
            newAgents[i] = agents[i].copy();
        }
        // empty boxes
        Box[] newBoxes = new Box[boxes.length];
        for (int i = 0; i < boxes.length; i++) {
            newBoxes[i] = boxes[i].copy();
        }
        State copy = new State(newAgents, newBoxes);
        copy.cmd = cmd;
        copy.parent = this;
        copy.g = g + 1;
        return copy;
    }

    public ArrayList<Command> extractPlan() {
        ArrayList<Command> plan = new ArrayList<>();
        State n = this;
        while (n.parent != null) {
            plan.add(n.getCommand());
            n = n.parent;
        }
        Collections.reverse(plan);
        return plan;
    }

    public boolean isGoalState() {
        // TODO: maybe update this to check for assigned boxes
        for (Goal goal : goals) {
            if (!boxMap.containsKey(goal.getPosition()))
                return false;
            if (getBox(goal.getPosition()).getLetter() != goal.getLetter())
                return false;
        }
        return true;
    }

    public Box getBox(Position pos) {
        Integer boxId = boxMap.getOrDefault(pos, null);
        return (boxId == null) ? null : boxes[boxId];
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
        if (_hash == 0) {
            int a = 0;
            for (Agent agent : this.agents) {
                a += agent.hashCode();
            }
            int b = 0;
            for (Box box : this.boxes) {
                b += box.hashCode();
            }
            _hash = a * b;
        }
        return _hash;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        char[][] level = new char[Y_MAX][X_MAX];
        for (int y = 0; y < Y_MAX; y++) {
            for (int x = 0; x < X_MAX; x++) {
                level[y][x] = walls[y][x] ? '+' : ' ';
            }
        }
        for (Goal goal : goals) {
            Position pos = goal.getPosition();
            level[pos.getY()][pos.getX()] = Character.toLowerCase(goal.getLetter());
            s.append("goal at: ");
            s.append(goal.toString());
            s.append("\n");
        }
        for (Box box : boxes) {
            Position pos = box.getPosition();
            level[pos.getY()][pos.getX()] = box.getLetter();
        }
        for (Agent agent : agents) {
            Position pos = agent.getPosition();
            level[pos.getY()][pos.getX()] = Character.forDigit(agent.getId(), 10);
        }
        for (int y = 0; y < Y_MAX; y++) {
            for (int x = 0; x < X_MAX; x++) {
                s.append(level[y][x]);
            }
            s.append("\n");
        }
        return s.toString();
    }
}
