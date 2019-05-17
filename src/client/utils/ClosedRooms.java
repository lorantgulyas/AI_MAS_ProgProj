package client.utils;

import client.state.*;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Implements methods for finding closed rooms in a level and later
 * to find which positions belong to which rooms. A closed room is
 * a section of the level that is blocked of from the rest of the level.
 */
public class ClosedRooms {

    private State initialState;
    private ArrayList<HashSet<Position>> rooms;

    public ClosedRooms(State initialState) {
        Level level = initialState.getLevel();
        this.initialState = initialState;
        this.rooms = this.findRooms(level);
    }

    /**
     * Finds the agents in each closed room.
     *
     * @param agents IDS of the agents to match.
     * @return An array of agents where the ith entry is the IDs of other agents that are in the same closed room as agent i.
     */
    public ArrayList<ArrayList<Agent>> findAgents(Agent[] agents) {
        // initialize with nulls
        ArrayList<ArrayList<Agent>> mapping = new ArrayList<>(agents.length);
        for (Agent agent : agents)
            mapping.add(null);

        for (Agent agent : agents) {
            ArrayList<Agent> mapTo = new ArrayList<>();
            Position pos = agent.getPosition();
            int id = agent.getId();
            for (Agent otherAgent: agents) {
                int otherID = otherAgent.getId();
                if (otherID != id && this.inSameRoom(otherAgent.getPosition(), pos)) {
                    mapTo.add(otherAgent);
                }
            }
            mapping.set(id, mapTo);
        }

        return mapping;
    }

    /**
     * Discovers whether two positions are in the same closed room or not.
     *
     * @param p1 First position.
     * @param p2 Second position.
     * @return Whether the two positions are in the same closed room.
     */
    public boolean inSameRoom(Position p1, Position p2) {
        for (HashSet<Position> room : this.rooms) {
            if (room.contains(p1) && room.contains(p2))
                return true;
        }
        return false;
    }

    /**
     * Creates sub-states for each closed room in the main level.
     *
     * @return List of states with different levels.
     */
    public ArrayList<SubState> getSubStates() {
        ArrayList<SubState> states = new ArrayList<>();
        for (HashSet<Position> room : this.rooms) {
            SubState state = new SubState(this.initialState, room);
            states.add(state);
        }
        return states;
    }

    private ArrayList<HashSet<Position>> findRooms(Level level) {
        ArrayList<HashSet<Position>> rooms = new ArrayList<>();
        ArrayList<Position> nonWallPositions = this.findNonWallPositions(level);
        HashSet<Position> nonWallSet = new HashSet<>(nonWallPositions);
        while (!nonWallPositions.isEmpty()) {
            Position exploreFrom = nonWallPositions.get(0);
            HashSet<Position> room = this.exploreRoom(nonWallPositions, nonWallSet, exploreFrom);
            rooms.add(room);
        }
        return rooms;
    }

    private ArrayList<Position> findNonWallPositions(Level level) {
        ArrayList<Position> nonWallPositions = new ArrayList<>();
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

    private HashSet<Position> exploreRoom(ArrayList<Position> nonWallPositions, HashSet<Position> nonWallSet, Position exploreFrom) {
        ArrayDeque<Position> frontier = new ArrayDeque<>();
        HashSet<Position> explored = new HashSet<>();
        frontier.add(exploreFrom);
        while (!frontier.isEmpty()) {
            Position pos = frontier.pop();
            explored.add(pos);

            nonWallPositions.remove(pos);
            nonWallSet.remove(pos);

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

        return explored;
    }

}
