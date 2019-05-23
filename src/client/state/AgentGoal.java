package client.state;

public class AgentGoal {

    private int agentID;
    private Position position;
    private int _hash;

    public AgentGoal(int agentID, Position position) {
        this.agentID = agentID;
        this.position = position;
        this._hash = this.computeHashCode();
    }

    public int getAgentID() {
        return agentID;
    }

    public Position getPosition() {
        return position;
    }

    @Override
    public String toString() {
        return "agent ID: " + this.agentID + ", position: " + position;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (this.getClass() != obj.getClass())
            return false;
        AgentGoal other = (AgentGoal) obj;
        return this.agentID == other.agentID
                && this.position.equals(other.position);
    }

    @Override
    public int hashCode() {
        return this._hash;
    }

    private int computeHashCode() {
        return this.position.hashCode() * (this.agentID + 1);
    }
}
