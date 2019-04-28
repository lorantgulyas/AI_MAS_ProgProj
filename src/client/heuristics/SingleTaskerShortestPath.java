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
        for (Box box : boxes) {
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
        Goal[] goals = state.getLevel().getGoals();
        ArrayList<Goal> unfulfilled = new ArrayList<>();
        for (Goal goal : goals) {
            Box box = state.getBoxAt(goal.getPosition());
            if (box == null || box.getLetter() != goal.getLetter()) {
                unfulfilled.add(goal);
            }
        }
        return unfulfilled;
    }

    private ArrayList<Box> getUnfinishedBoxes(State state) {
        Level level = state.getLevel();
        ArrayList<Box> unfinished = new ArrayList<>();
        Box[] boxes = state.getBoxes();
        for (Box box : boxes) {
            Goal goal = level.getGoalAt(box.getPosition());
            if (goal == null || box.getLetter() != goal.getLetter()) {
                unfinished.add(box);
            }
        }
        return unfinished;
    }

    private ArrayList<Goal> getAgentGoals(ArrayList<Goal> goals, ArrayList<Box> boxes, Agent agent) {
        ArrayList<Goal> agentGoals = new ArrayList<>();
        for (Goal goal : goals) {
            for (Box box : boxes) {
                if (box.getLetter() == goal.getLetter() && box.getColor() == agent.getColor()) {
                    agentGoals.add(goal);
                }
            }
        }
        return agentGoals;
    }

    private int agentHeuristic(ArrayList<Goal> goals, ArrayList<Box> boxes, Agent agent) {
        goals = this.getAgentGoals(goals, boxes, agent);
        int nGoals = goals.size();
        if (nGoals == 0) {
            return 0;
        }
        int sum = 0;
        int minAgent2BoxDistance = Integer.MAX_VALUE;
        int minWalkDistance = Integer.MAX_VALUE;
        for (Goal goal : goals) {
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
        return minAgent2BoxDistance == Integer.MAX_VALUE
                ? sum + nGoals * this.measurer.getV()
                : minAgent2BoxDistance + sum + nGoals * this.measurer.getV();
    }

    public int h(State n) {
        ArrayList<Goal> goals = this.getUnfulfilledGoals(n);
        if (goals.size() == 0) {
            return 0;
        }
        ArrayList<Box> boxes = this.getUnfinishedBoxes(n);
        Agent[] agents = n.getAgents();
        int h = 0;
        for (Agent agent : agents) {
            h += this.agentHeuristic(goals, boxes, agent);
        }
        return h;
    }
}
