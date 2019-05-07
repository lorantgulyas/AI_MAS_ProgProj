package client.policies;

import client.definitions.AMessagePolicy;
import client.distance.BFS;
import client.graph.Action;
import client.graph.Plan;
import client.state.*;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Policy that is described in the multi-agent A* paper.
 * It only sends messages to other agents whose public variables
 * are somehow affected by an action.
 */
public class PublicActionsPolicy extends AMessagePolicy {

    private BroadcastPolicy broadcast;
    private ArrayList<ArrayList<Integer>> agentMapping;

    public PublicActionsPolicy(State initialState) {
        super(initialState);
        this.broadcast = new BroadcastPolicy(initialState);
        this.agentMapping = this.findAgentRoomMapping(initialState);
    }

    @Override
    public Iterable<Integer> receivers(Plan node, int sender) {
        if (this.someGoalHasChanged(node, sender))
            return this.broadcast.receivers(node, sender);

        return this.agentMapping.get(sender);
    }

    private boolean someGoalHasChanged(Plan node, int sender) {
        Plan parent = node.getParent();
        if (parent == null)
            return false;
        State parentState = parent.getState();
        State state = node.getState();
        Level level = state.getLevel();
        Goal[] agentGooals = level.getAgentGoals(sender);
        for (Goal agentGoal : agentGooals) {
            Position goalPosition = agentGoal.getPosition();
            char goalLetter = agentGoal.getLetter();
            Box box = state.getBoxAt(goalPosition);
            Box parentBox = parentState.getBoxAt(goalPosition);
            boolean reachedGoal = box != null && box.getLetter() == goalLetter;
            boolean parentReachedGoal = parentBox != null && parentBox.getLetter() == goalLetter;
            if ((reachedGoal && !parentReachedGoal) || (!reachedGoal && parentReachedGoal)) {
                return true;
            }
        }
        return false;
    }

    private ArrayList<ArrayList<Integer>> findAgentRoomMapping(State initialState) {
        Agent[] agents = initialState.getAgents();

        // initialize with nulls
        ArrayList<ArrayList<Integer>> mapping = new ArrayList<>(agents.length);
        for (Agent agent : agents)
            mapping.add(null);

        // find agents in the same room
        ArrayList<Position> nonWallPositions = this.findNonWallPositions(initialState);
        HashSet<Position> nonWallSet = new HashSet<>(nonWallPositions);
        while (!nonWallPositions.isEmpty()) {
            Position exploreFrom = nonWallPositions.get(0);
            this.exploreRoom(initialState, nonWallPositions, nonWallSet, mapping, exploreFrom);
        }
        return mapping;
    }

    private ArrayList<Position> findNonWallPositions(State state) {
        ArrayList<Position> nonWallPositions = new ArrayList<>();
        Level level = state.getLevel();
        boolean[][] walls = level.getWalls();
        for (int i = 0; i < walls.length; i++) {
            boolean[] row = walls[i];
            for (int j = 0; j < row.length; j++) {
                if (!row[j]) {
                    Position position = new Position(i, j);
                    nonWallPositions.add(position);
                }
            }
        }
        return nonWallPositions;
    }

    private void exploreRoom(
            State state,
            ArrayList<Position> nonWallPositions,
            HashSet<Position> nonWallSet,
            ArrayList<ArrayList<Integer>> mapping,
            Position exploreFrom
    ) {
        ArrayList<Integer> agents = new ArrayList<>();

        // explore room through a breadth first search
        ArrayDeque<Position> frontier = new ArrayDeque<>();
        HashSet<Position> explored = new HashSet<>();
        frontier.add(exploreFrom);
        while (!frontier.isEmpty()) {
            Position pos = frontier.pop();
            explored.add(pos);

            nonWallPositions.remove(pos);
            nonWallSet.remove(pos);

            if (state.agentAt(pos))
                agents.add(state.getAgentAt(pos).getId());

            Position north = pos.north();
            Position east = pos.east();
            Position south = pos.south();
            Position west = pos.west();

            if (!explored.contains(north) && !frontier.contains(north) && nonWallSet.contains(north))
                frontier.add(north);

            if (!explored.contains(east) && !frontier.contains(east) && nonWallSet.contains(east))
                frontier.add(east);

            if (!explored.contains(south) && !frontier.contains(south) && nonWallSet.contains(south))
                frontier.add(south);

            if (!explored.contains(west) && !frontier.contains(west) && nonWallSet.contains(west))
                frontier.add(west);
        }

        // create agent mapping
        for (Integer agentID : agents) {
            ArrayList<Integer> mapTo = new ArrayList<>();
            for (Integer otherAgentID : agents) {
                if (!agentID.equals(otherAgentID))
                    mapTo.add(otherAgentID);
            }
            mapping.set(agentID, mapTo);
        }
    }

    @Override
    public String toString() {
        return "public-actions";
    }
}
