package client.heuristics;

import client.definitions.ADistance;
import client.definitions.AHeuristic;
import client.state.*;

import java.util.Arrays;
import java.util.HashMap;

public class GoalSeeker extends AHeuristic {

    private ADistance measurer;
    private Level level;
    private HashMap<Goal, Integer> goalBoxMap;

    public GoalSeeker(State initialState, ADistance measurer) {
        super(initialState);
        this.measurer = measurer;
        this.level = initialState.getLevel();
        this.goalBoxMap = this.getGoalBoxMap(initialState);
    }

    @Override
    public int h(State state) {
        int h = 0;
        h += this.sumOfGoals(state);
        h += this.sumOfAgentEndPositions(state);
        return h;
    }

    @Override
    public String toString() {
        return "goal-seeker";
    }

    private HashMap<Goal, Integer> getGoalBoxMap(State initialState) {
        HashMap<Goal, Integer> map = new HashMap<>();
        Goal[] goals = initialState.getLevel().getGoals();
        Box[] boxes = initialState.getBoxes();
        boolean[] usedBoxes = new boolean[boxes.length];
        Arrays.fill(usedBoxes, false);
        for (Goal goal : goals) {
            Box box = this.findClosestBoxToGoal(initialState, goal, usedBoxes);
            usedBoxes[box.getId()] = true;
            map.put(goal, box.getId());
        }
        return map;
    }

    private int sumOfGoals(State state) {
        int h = 0;
        Box[] boxes = state.getBoxes();
        Goal[] goals = this.level.getGoals();
        for (Goal goal : goals) {
            int boxID = this.goalBoxMap.get(goal);
            Box box = boxes[boxID];
            if (!box.getPosition().equals(goal.getPosition())) {
                // important to multiply to make it more "profitable" to reach a goal than to be close to a box
                h += 3 * this.measurer.distance(state, box.getPosition(), goal.getPosition());
                Agent agent = this.findClosestAgentToBox(state, box);
                if (agent != null) {
                    // important to multiply to make it more "profitable" to reach a goal
                    // than to be close to an end position
                    h += 2 * this.measurer.distance(state, agent.getPosition(), box.getPosition());
                }
            }
        }
        return h;
    }

    private int sumOfAgentEndPositions(State state) {
        int h = 0;
        Agent[] agents = state.getAgents();
        for (AgentGoal agentEndPosition : this.level.getAgentEndPositions()) {
            Agent agent = agents[agentEndPosition.getAgentID()];
            h += this.measurer.distance(state, agent.getPosition(), agentEndPosition.getPosition());
        }
        return h;
    }

    private Agent findClosestAgentToBox(State state, Box box) {
        Agent[] agents = state.getAgents();
        Agent closestAgent = null;
        int minDistance = Integer.MAX_VALUE;
        for (Agent agent : agents) {
            if (agent.getColor() == box.getColor()) {
                int distance = this.measurer.distance(state, agent.getPosition(), box.getPosition());
                if (closestAgent == null || distance < minDistance) {
                    closestAgent = agent;
                    minDistance = distance;
                }
            }
        }
        return closestAgent;
    }

    private Box findClosestBoxToGoal(State state, Goal goal, boolean[] usedBoxes) {
        Box[] boxes = state.getBoxes();
        Box closestBox = null;
        int minDistance = Integer.MAX_VALUE;
        for (Box box : boxes) {
            if (!usedBoxes[box.getId()] && box.getLetter() == goal.getLetter()) {
                int distance = this.measurer.distance(state, box.getPosition(), goal.getPosition());
                if (closestBox == null || distance < minDistance) {
                    closestBox = box;
                    minDistance = distance;
                }
            }
        }
        return closestBox;
    }
}
