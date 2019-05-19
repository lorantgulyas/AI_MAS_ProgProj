package client.strategies.serialized_astar;

import client.definitions.AHeuristic;
import client.graph.Plan;
import client.state.*;

import java.util.*;
import java.util.stream.Collectors;

public class Floodfill extends AHeuristic implements Comparator<Plan> {

    private int[][][][] matrix;
    private boolean[][] rooms;

    private Level level;
    private int yMax;
    private int xMax;

    public static final int UNDISCOVERED = Integer.MAX_VALUE;

    public Floodfill(State initialState) {
        super(initialState);
        this.level = initialState.getLevel();
        this.xMax = level.getColCount();
        this.yMax = level.getRowCount();

        // matrix starts with goal coordinates, in order to make the memory layout nicer
        matrix = new int[yMax][xMax][yMax][xMax];
        for (int goalY = 0; goalY < yMax; goalY++) {
            for (int goalX = 0; goalX < xMax; goalX++) {
                fillSubMatrix(goalY, goalX);
            }
        }
    }

    private int getX(Position pos) {
        return pos.getCol();
    }

    private int getY(Position pos) {
        return pos.getRow();
    }

    private void fillSubMatrix(int goalY, int goalX) {
        boolean[][] walls = level.getWalls();

        // preemptively fill all values with -1
        for (int[] rowTo : matrix[goalY][goalX])
            Arrays.fill(rowTo, UNDISCOVERED);

        // if goal position is not wall
        if (!walls[goalX][goalY]) {
            int currentDistance = 0;
            matrix[goalY][goalX][goalY][goalX] = currentDistance;
            ArrayDeque<Position> toExplore = new ArrayDeque<>();
            Position pos = new Position(goalX, goalY);
            toExplore.add(pos);
            while (!toExplore.isEmpty()) {
                pos = toExplore.pop();
                int x = getX(pos), y = getY(pos);
                currentDistance = matrix[goalY][goalX][y][x] + 1;

                // ugly shit
                if (!walls[x][y - 1] && matrix[goalY][goalX][y - 1][x] == UNDISCOVERED) {
                    matrix[goalY][goalX][y - 1][x] = currentDistance;
                    toExplore.add(new Position(x, y - 1));
                }
                if (!walls[x][y + 1] && matrix[goalY][goalX][y + 1][x] == UNDISCOVERED) {
                    matrix[goalY][goalX][y + 1][x] = currentDistance;
                    toExplore.add(new Position(x, y + 1));
                }
                if (!walls[x - 1][y] && matrix[goalY][goalX][y][x - 1] == UNDISCOVERED) {
                    matrix[goalY][goalX][y][x - 1] = currentDistance;
                    toExplore.add(new Position(x - 1, y));
                }
                if (!walls[x + 1][y] && matrix[goalY][goalX][y][x + 1] == UNDISCOVERED) {
                    matrix[goalY][goalX][y][x + 1] = currentDistance;
                    toExplore.add(new Position(x + 1, y));
                }
            }
        }
    }

    public void findRooms() {
        // return if already preprocessed
        if (rooms != null) return;

        boolean[][] walls = level.getWalls();
        // TODO: fallback on storages, when there are no rooms (half plus on map)
        rooms = new boolean[this.yMax][this.xMax];
        // prefill
        for (int y = 0; y < this.yMax; y++)
            for (int x = 0; x < this.xMax; x++)
                rooms[y][x] = false;


        // detect rooms that can't have goal positions
        Goal[] goals = level.getGoals();
        HashMap<Position, Goal> goalMap = new HashMap<>();
        for (Goal goal : goals) {
            goalMap.put(goal.getPosition(), goal);
        }
        for (int y = 1; y < this.yMax - 2; y++) {
            for (int x = 1; x < this.xMax - 2; x++) {
                // 2x2 window
                // XX
                // XX
                if (!(walls[x][y] || walls[x + 1][y] || walls[x][y + 1] || walls[x + 1][y + 1])) {
                    if (!(goalMap.containsKey(new Position(x, y)) ||
                            goalMap.containsKey(new Position(x + 1, y)) ||
                            goalMap.containsKey(new Position(x, y + 1)) ||
                            goalMap.containsKey(new Position(x + 1, y + 1)))) {
                        rooms[y][x] = rooms[y][x + 1] = rooms[y + 1][x] = rooms[y + 1][x + 1] = true;
                    }
                }
            }
        }

        // fallback 1 - detect rooms that have goal positions
        if (!hasRooms(rooms)) {
            System.err.println("Fallback 1 rooms");
            for (int y = 1; y < this.yMax - 2; y++) {
                for (int x = 1; x < this.xMax - 2; x++) {
                    // 2x2 window
                    if (!(walls[x][y] || walls[x + 1][y] || walls[x][y + 1] || walls[x + 1][y + 1])) {
                        rooms[y][x] = rooms[y][x + 1] = rooms[y + 1][x] = rooms[y + 1][x + 1] = true;
                    }
                }
            }
        }

        // fallback 2 - corners
        if (!hasRooms(rooms)) {
            System.err.println("Fallback 2 rooms");
            for (int y = 1; y < this.yMax - 1; y++) {
                for (int x = 1; x < this.xMax - 1; x++) {
                    // corner 3 out of 1
                    //  0
                    // 0X0
                    //  0
                    int freeNeighbors = 0;
                    if (!walls[x][y] && !goalMap.containsKey(new Position(x, y))) {
                        if (!walls[x][y - 1] && !goalMap.containsKey(new Position(x, y - 1)))
                            freeNeighbors++;
                        if (!walls[x][y + 1] && !goalMap.containsKey(new Position(x, y + 1)))
                            freeNeighbors++;
                        if (!walls[x - 1][y] && !goalMap.containsKey(new Position(x - 1, y)))
                            freeNeighbors++;
                        if (!walls[x + 1][y] && !goalMap.containsKey(new Position(x + 1, y)))
                            freeNeighbors++;
                        if (freeNeighbors > 2)
                            rooms[y][x] = true;
                    }
                }
            }
        }

        // print
        StringBuilder s = new StringBuilder();
        for (int y = 0; y < this.yMax; y++) {
            for (int x = 0; x < this.xMax; x++) {
                if (rooms[y][x]) {
                    s.append("O");
                } else if (walls[x][y]) {
                    s.append("X");
                } else {
                    s.append(" ");
                }
            }
            s.append("\n");
        }
        System.err.println(s.toString());
    }

    boolean hasRooms(boolean[][] rooms) {
        for (boolean[] col : rooms) {
            for (boolean b : col) {
                if (b) return true;
            }
        }
        return false;
    }

    public void prioritizeGoals() {
        for (Goal goal : level.getGoals()) {
            Position pos = goal.getPosition();
            int priority = Integer.MAX_VALUE;
            for (int y = 0; y < this.yMax; y++) {
                for (int x = 0; x < this.xMax; x++) {
                    if (rooms[y][x] && (matrix[getY(pos)][getX(pos)][y][x] < priority)) {
                        priority = matrix[getY(pos)][getX(pos)][y][x];
                    }
                }
            }
            if (priority != Integer.MAX_VALUE) {
                goal.setPriority(priority);
            }
        }

        Arrays.sort(level.getGoals(), Comparator.comparingInt(Goal::getPriority).reversed());
        // workaround for mutation :(
        level.setGoals(level.getGoals());
        // print
//        for (Goal goal : State.getGoals()) {
//            System.err.println(goal);
//        }
    }

    public Box[] prioritizeBoxes(Box[] boxes) {
        // TODO: this works only for initial preprocessing, make it work for each subgoal
        Goal[] goals = level.getGoals();
        HashSet<Box> boxSet = new HashSet<>(Arrays.asList(boxes));
        ArrayList<Box> boxList = new ArrayList<>();

        // assign boxes to goals in correct order
        for (int i = 0; i < goals.length; i++) {
            int minDistance = Integer.MAX_VALUE;
            Box minBox = boxSet.iterator().next();
            for (Box box : boxes) {
                if (boxSet.contains(box) &&
                        box.getLetter() == goals[i].getLetter() &&
                        minDistance > distance(box.getPosition(), goals[i].getPosition())) {
                    minBox = box;
                    minDistance = distance(box.getPosition(), goals[i].getPosition());
                }
            }
            boxSet.remove(minBox);
            boxList.add(minBox);
        }

        // append all remaining
        boxList.addAll(boxSet);

        return boxList.toArray(Box[]::new);
    }

    public ArrayList<Position> findPath(Position goalPos, Position boxPos) {
        HashMap<Position, Integer> goalMap = level.getGoalIndexMap();
        // miniAstar!
        Position[] moves = {
                new Position(0, -1),
                new Position(1, 0),
                new Position(0, 1),
                new Position(-1, 0)
        };
        ArrayList<Step> path = new ArrayList<>();
        boolean[][] walls = level.getWalls();
        Step init = new Step(boxPos, 0,
                distance(boxPos, goalPos), null);
        PriorityQueue<Step> frontier = new PriorityQueue<>(100, init);
        HashSet<Position> frontierSet = new HashSet<>();
        HashSet<Position> explored = new HashSet<>();
        frontier.add(init);
        frontierSet.add(init.position);

        while (!frontier.isEmpty()) {
            Step leaf = frontier.poll();
            frontierSet.remove(leaf.position);

            // found our goal
            if (leaf.position.equals(goalPos)) {
                while (leaf.parent != null) {
                    path.add(leaf.parent); // don't add the goal pos itself
                    leaf = leaf.parent;
                }
                break;
            }

            // expand our states
            explored.add(leaf.position);
            for (Position move : moves) {
                Position newPos = leaf.position.add(move);
                // if neither wall nor explored
                if (!explored.contains(newPos) && !frontierSet.contains(newPos) &&
                        !walls[getX(newPos)][getY(newPos)]) {
                    int stepCost = goalMap.containsKey(newPos) ? 10 : 1;
                    if (newPos.equals(goalPos)) {
                        stepCost = 0;
                    }
                    Step newStep = new Step(newPos, leaf.g + stepCost,
                            distance(newPos, goalPos), leaf);
                    frontier.add(newStep);
                    frontierSet.add(newPos);
                }
            }
        }

        return path.stream()
                .map(s -> s.position)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public State goalDependencies(State state) {
        Box[] boxes = state.getBoxes();
        Goal[] goals = level.getGoals();
        HashMap<Position, Integer> goalMap = level.getGoalIndexMap();
        HashMap<Position, HashSet<Position>> taskMap = new HashMap<>();
        for (int i = 0; i < goals.length; i++) {
            taskMap.put(goals[i].getPosition(), new HashSet<>());
        }

        // for each goal prioritized on depth
        for (int i = 0; i < goals.length; i++) {
            Goal goal = goals[i];

            // find the closest box
            // it has the same index in the boxes array, if it already has local priority
            Box box = boxes[i];

            // find a path from box to goal (possibly shortest, but avoiding goals)
            ArrayList<Position> path = findPath(goal.getPosition(), box.getPosition());

            // check if path has goals on it, then add it to those goals as dependency
            for (Position pos : path) {
                if (goalMap.containsKey(pos)) {
                    HashSet<Position> posSet = taskMap.getOrDefault(pos, new HashSet<>());
                    posSet.add(goal.getPosition());
                    taskMap.put(pos, posSet);
                }
            }
        } // end of goal iteration

        // log dependency map
//        for (Map.Entry<Position, HashSet<Position>> entry : taskMap.entrySet()) {
//            System.err.print(goals[goalMap.get(entry.getKey())].getLetter() + " has dependents: ");
//            for (Position depPos : entry.getValue()) {
//                System.err.print(goals[goalMap.get(depPos)].getLetter() + ", ");
//            }
//            System.err.print("\n");
//        }

        // prioritize tasks
        ArrayList<Position> ordered = new ArrayList<>();
        HashSet<Position> visited = new HashSet<>();

        while (taskMap.size() != 0) {
            ArrayList<Position> minPos = null;
            int minDependents = Integer.MAX_VALUE;

            // iterate taskMap
            Iterator<Map.Entry<Position, HashSet<Position>>> iter = taskMap.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<Position, HashSet<Position>> entry = iter.next();
                HashSet<Position> val = entry.getValue();
                val.removeAll(visited);
                // find minimum
                if (val.size() < minDependents) {
                    minDependents = val.size();
                    minPos = new ArrayList<>();
                    minPos.add(entry.getKey());
                } else if (val.size() == minDependents) {
                    minPos.add(entry.getKey());
                }
            }

            // add mindependents to ordered
            minPos.sort(Comparator
                    .comparingInt(p -> goals[goalMap.get(p)].getPriority())
                    .reversed());
            ordered.addAll(minPos);
            visited.addAll(minPos);
            taskMap.keySet().removeAll(minPos);
        }

        // extend boxes with unused
        Goal[] newGoals = new Goal[goals.length];
        ArrayList<Box> newBoxes = new ArrayList<>();
        for (int i = 0; i < goals.length; i++) {
            int oldIndex = goalMap.get(ordered.get(i));
            newGoals[i] = goals[oldIndex];
            newBoxes.add(boxes[oldIndex]);
        }
        HashSet<Box> allBoxes = new HashSet<>(Arrays.asList(boxes));
        allBoxes.removeAll(newBoxes);
        newBoxes.addAll(allBoxes);
        Box[] unusedBoxes = newBoxes.toArray(Box[]::new);

        level.setGoals(newGoals);
        state.setBoxes(unusedBoxes);
        return state;
    }

    private class Step implements Comparator<Step> {
        public Position position;
        public int g;
        public int h;
        public Step parent;

        public Step(Position position, int g, int h, Step parent) {
            this.position = position;
            this.g = g;
            this.h = h;
            this.parent = parent;
        }

        @Override
        public int compare(Step s1, Step s2) {
            return s1.g + s1.h - s2.g - s2.h;
        }
    }

    public void printMatrix(int goalX, int goalY) {
        StringBuilder s = new StringBuilder();

        for (int y = 0; y < this.yMax; y++) {
            for (int x = 0; x < this.xMax; x++) {
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
        return matrix[getY(to)][getX(to)][getY(from)][getX(from)];
    }

    @Override
    public int compare(Plan n1, Plan n2) {
        // greedy
//        return h(n1) - h(n2);
        // A*
        return n1.g() + h(n1.getState()) - n2.g() - h(n2.getState());
    }

    public int h(State state) {
        int h = 0;
        Agent[] agents = state.getAgents();
        Goal[] goals = level.getGoals();
        Box[] boxes = state.getBoxes();

        // admissible
//        for (int i = 0; i < goals.length; i++) {
//            h += distance(goals[i].getPosition(), boxes[i].getPosition());
//        }

        for (int i = 0; i < goals.length; i++) {
            h += distance(goals[i].getPosition(), boxes[i].getPosition()) *
                    // not admissible
                    // experimental heuristics
                    (i == (goals.length - 1) ? 1 : 10);
        }

        h += distance(boxes[goals.length - 1].getPosition(), agents[0].getPosition());
        return h;
    }

    public int hPath(State state, ArrayList<Position> positions) {
        int h = 0;
        Position agentPos = state.getAgents()[0].getPosition();
        for (Position pos : positions) {
            if (state.getBoxAt(pos) != null) {
                h += 10 + distance(pos, agentPos);
            }
        }
        return h;
    }
}