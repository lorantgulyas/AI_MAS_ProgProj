package client.state;

public class Agent {
    private int id;
    private Color color;
    private Position position;

    public Agent(int id, Color color, Position position) {
        this.id = id;
        this.color = color;
        this.position = position;
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
        return this.position.hashCode() * (this.id + 1);
    }
}
