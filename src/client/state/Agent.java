package client.state;

// TODO: add color to agent
public class Agent {
    private int id;
    private Color color;
    private Position position;
    private int _hash;

    public Agent(int id, Color color, Position position) {
        this.id = id;
        this.color = color;
        this.position = position;
        this._hash = this.computeHashCode();
    }

    public int getId() {
        return id;
    }

    public Color getColor() {
        return color;
    }

    public Position getPosition() {
        return position;
    }

    @Override
    public String toString() {
        return "id: " + id + ", color: " + color + ", position: " + position;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (this.getClass() != obj.getClass())
            return false;
        Agent other = (Agent) obj;
        return this.id == other.id
                && this.color == other.color
                && this.position.equals(other.position);
    }

    @Override
    public int hashCode() {
        return this._hash;
    }

    private int computeHashCode() {
        // plus one to avoid multiplying by zero
        return (this.id + 1) * (this.color.ordinal() + 1) * (this.position.hashCode() + 1);
    }
}
