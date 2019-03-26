package client;

public class Agent {
    private final int id;
    private final Color color;
    private Position position;

    public Agent (int id, Color color, Position position) {
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
}
