package client;

public class Box {
    private char letter;
    private Color color;
    private Position position;

    public Box (char letter, Color color, Position position) {
        this.letter = letter;
        this.color = color;
        this.position = position;
    }

    public Box (char letter, Color color) {
        this.letter = letter;
        this.color = color;
        this.position = null;
    }

    public char getLetter() {
        return letter;
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
        return "char: " + letter + ", color: " + color + ", position: " + position;
    }
}
