package client.graph;

import client.state.Position;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Action {

    private int agentID;
    private Command command;
    private Set<Position> cellsUsed;

    public Action(int agentID, Command command, Set<Position> cellsUsed) {
        this.agentID = agentID;
        this.command = command;
        this.cellsUsed = cellsUsed;
    }

    public Action(int agentID, Command command, Position[] cellsUsed) {
        this.agentID = agentID;
        this.command = command;
        this.cellsUsed = new HashSet<>(cellsUsed.length);
        this.cellsUsed.addAll(Arrays.asList(cellsUsed));
    }

    public int getAgentID() {
        return agentID;
    }

    public Set<Position> getCellsUsed() {
        return cellsUsed;
    }

    public Command getCommand() {
        return command;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.agentID);
        builder.append(":");
        builder.append(this.command.toString());
        return builder.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (obj.getClass() != this.getClass())
            return false;
        Action other = (Action) obj;
        return other.agentID == this.agentID
                && other.getCommand().equals(this.command);
    }

    @Override
    public int hashCode() {
        return agentID + this.command.hashCode();
    }
}
