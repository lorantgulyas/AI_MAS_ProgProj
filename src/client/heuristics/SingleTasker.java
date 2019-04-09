package client.heuristics;

import client.definitions.AHeuristic;
import client.distance.LazyShortestPath;
import client.state.*;

import java.util.ArrayList;

public class SingleTasker extends AHeuristic {
    private LazyShortestPath measurer;

    public SingleTasker(State initialState) {
        super(initialState);
        this.measurer = new LazyShortestPath(initialState);
    }

    private Box getClosestBoxToGoal(ArrayList<Box> boxes, Goal goal) {
        Box closest = null;
        int minDistance = Integer.MAX_VALUE;
        int n = boxes.size();
        for (int i = 0; i < n; i++) {
            Box box = boxes.get(i);
            if (box.getLetter() == goal.getLetter()) {
                int distance = this.measurer.distance(box.getPosition(), goal.getPosition());
                if (distance < minDistance) {
                    closest = box;
                    minDistance = distance;
                }
            }
        }
        return closest;
    }

    private ArrayList<Goal> getUnfulfilledGoals(State state) {
        Goal[] goals = state.getGoals();
        ArrayList<Goal> unfulfilled = new ArrayList<>();
        for (int i = 0; i < goals.length; i++) {
            Goal goal = goals[i];
            Box box = state.getBoxAt(goal.getPosition());
            if (box != null && box.getLetter() != goal.getLetter()) {
                unfulfilled.add(goal);
            }
        }
        return unfulfilled;
    }

    private ArrayList<Box> getUnfinishedBoxes(State state) {
        ArrayList<Box> unfulfilled = new ArrayList<>();
        Box[] boxes = state.getBoxes();
        Goal[] goals = state.getGoals();
        for (Box box : boxes) {
            for (Goal goal : goals) {
                if (box.getLetter() != goal.getLetter()) {
                    unfulfilled.add(box);
                }
            }
        }
        return unfulfilled;
    }

    public int h(State n) {
        ArrayList<Goal> goals = this.getUnfulfilledGoals(n);
        int nGoals = goals.size();
        if (nGoals == 0) {
            return 0;
        }
        ArrayList<Box> boxes = this.getUnfinishedBoxes(n);
        // TODO: enable multi agent support!
        Agent agent = n.getAgents()[0];
        int sum = 0;
        int minAgent2BoxDistance = Integer.MAX_VALUE;
        int minWalkDistance = Integer.MAX_VALUE;
        for (int i = 0; i < nGoals; i++) {
            Goal goal = goals.get(i);
            Box box = this.getClosestBoxToGoal(boxes, goal);
            if (box != null) {
                int box2goalDistance = this.measurer.distance(box.getPosition(), goal.getPosition());
                int agent2boxDistance = this.measurer.distance(agent.getPosition(), box.getPosition());
                int walkDistance = agent2boxDistance + box2goalDistance;
                sum += box2goalDistance;
                if (walkDistance < minWalkDistance) {
                    minAgent2BoxDistance = agent2boxDistance;
                    minWalkDistance = walkDistance;
                }
            }
        }
        if (minAgent2BoxDistance == Integer.MAX_VALUE) {
            return sum + nGoals * this.measurer.getV();
        } else {
            return minAgent2BoxDistance + sum + nGoals * this.measurer.getV();
        }
    }
}
