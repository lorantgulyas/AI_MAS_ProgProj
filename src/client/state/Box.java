package client.state;

public class Box {
    private char letter;
    private Color color;
    private Position position;

    public Box(char letter, Color color, Position position) {
        this.letter = letter;
        this.color = color;
        this.position = position;
    }

    public Box(char letter, Color color) {
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

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (this.getClass() != obj.getClass())
            return false;
        Box other = (Box) obj;
        if (this.letter != other.letter)
            return false;
        if (this.color != other.color)
            return false;
        if (this.position != other.position)
            return false;
        return true;
    }
}
