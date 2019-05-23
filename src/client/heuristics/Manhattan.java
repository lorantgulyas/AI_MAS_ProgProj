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
        Level level = state.getLevel();
        Agent[] agents = state.getAgents();
        Box[] boxes = state.getBoxes();
        for (Box box : boxes) {
            Position boxPos = box.getPosition();
            for (Agent agent : agents) {
                if (agent.getColor() == box.getColor()) {
                    h += this.measurer.distance(state, agent.getPosition(), boxPos);
                    Goal[] goals = level.getAgentGoals(agent.getId());
                    for (Goal goal : goals) {
                        if (goal.getLetter() == box.getLetter()) {
                            h += this.measurer.distance(state, boxPos, goal.getPosition());
                        }
                    }
                }
            }
        }
        for (AgentGoal agentEndPosition : level.getAgentEndPositions()) {
            Agent agent = agents[agentEndPosition.getAgentID()];
            h += this.measurer.distance(state, agent.getPosition(), agentEndPosition.getPosition());
        }
        return h;
    }

    @Override
    public String toString() {
        return "manhattan";
    }
}
