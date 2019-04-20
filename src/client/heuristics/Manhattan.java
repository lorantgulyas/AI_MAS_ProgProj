package client.heuristics;

import client.definitions.AHeuristic;
import client.distance.LazyManhattan;
import client.state.*;

public class Manhattan extends AHeuristic {

    private LazyManhattan measurer;

    public Manhattan(State initialState) {
        super(initialState);
        this.measurer = new LazyManhattan();
    }

    @Override
    public int h(State state) {
        int h = 0;
        Agent[] agents = state.getAgents();
        Box[] boxes = state.getBoxes();
        Goal[] goals = state.getLevel().getGoals();
        for (Goal goal : goals) {
            for (Agent agent : agents) {
                h += this.measurer.distance(goal.getPosition(), agent.getPosition());
            }
        }
        for (Box box : boxes) {
            for (Agent agent : agents) {
                h += this.measurer.distance(box.getPosition(), agent.getPosition());
            }
        }
        return h;
    }
}
