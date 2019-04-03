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

    public char getLetter() {
        return letter;
    }

    public Color getColor() {
        return color;
    }

    public Position getPosition() {
        return position;
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
        return this.letter == other.letter
                && this.color == other.color
                && this.position.equals(other.position);
    }

    @Override
    public int hashCode() {
        return this.position.hashCode() + this.letter + this.color.ordinal();
    }
}
