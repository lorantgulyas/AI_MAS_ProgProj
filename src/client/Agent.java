package client;

public class Agent {
    private int number;
    private Color color;
    private Position position;

    public Agent(int number, Color color, Position position) {
        this.number = number;
        this.color = color;
        this.position = position;
    }

    public int getNumber() {
        return number;
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
}
