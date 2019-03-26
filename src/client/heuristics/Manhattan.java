package client.heuristics;

import client.*;
import client.definitions.AHeuristic;
import client.definitions.AState;

import java.lang.Math;

public class Manhattan extends AHeuristic {

    public Manhattan(AState initialState) {
        super(initialState);
    }

    @Override
    public int h(AState state) {
        int h = 0;
        Agent[] agents = state.getAgents();
        Box[] boxes = state.getBoxes();
        Goal[] goals = state.getGoals();
        for (Goal goal : goals) {
            for (Agent agent : agents) {
                h += this.distance(goal.getPosition(), agent.getPosition());
            }
        }
        for (Box box : boxes) {
            for (Agent agent : agents) {
                h += this.distance(box.getPosition(), agent.getPosition());
            }
        }
        return h;
    }

    private int distance(Position p1, Position p2) {
        return Math.abs(p1.getRow() - p2.getRow()) + Math.abs(p1.getCol() - p2.getCol());
    }
}
