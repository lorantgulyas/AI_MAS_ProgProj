package client;

public class Agent {
    private int id;
    private Color color;
    private Position position;

    public Agent(int id, Color color, Position position) {
        this.id = id;
        this.color = color;
        this.position = position;
    }

    public Agent (int id, Color color) {
        this.id = id;
        this.color = color;
        this.position = null;
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

    public void setPosition(Position position) {
        this.position = position;
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
        if (this.id != other.id)
            return false;
        if (this.color != other.color)
            return false;
        if (this.position != other.position)
            return false;
        return true;
    }
}
