package client;

import client.definitions.AState;

public class State extends AState {
    private static boolean[][] walls;
    private Agent[] agents;
    private Box[] boxes;
    private Goal[] goals;

    public State(Agent[] agents, Box[] boxes, Goal[] goals) {
        this.agents = agents;
        this.boxes = boxes;
        this.goals = goals;
    }

    public static void setWalls(boolean[][] initialWalls) {
        walls = initialWalls;
    }

    public boolean[][] getWalls() {
        return walls;
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
}