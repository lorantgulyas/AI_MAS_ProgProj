package client.heuristics.unblocker;

import client.path.AllObjectsAStar;
import client.path.Node;
import client.path.WallOnlyAStar;
import client.state.*;

import java.util.ArrayList;

class PathHelper {

    static ArrayList<Position> getPath(WallOnlyAStar planner, State state, AgentGoal agentEndPosition, Agent agent) {
        Node result = planner.plan(state, agent.getPosition(), agentEndPosition.getPosition(), agent.getColor());
        return result.path();
    }

    static ArrayList<Position> getPath(WallOnlyAStar planner, State state, Goal goal, Box box, Agent agent) {
        Node agentResult = planner.plan(state, agent.getPosition(), box.getPosition(), agent.getColor());
        Node goalResult = planner.plan(state, box.getPosition(), goal.getPosition());
        ArrayList<Position> path = new ArrayList<>();
        if (agentResult != null)
            path.addAll(agentResult.path());
        if (goalResult != null)
            path.addAll(goalResult.path());
        return path;
    }

    static boolean isBlocked(AllObjectsAStar planner, State state, Agent agent, Position from, Position to) {
        Node result = planner.plan(state, from, to, agent.getColor(), 4);
        return result == null;
    }

}
