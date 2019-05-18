package subgoaler;

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
        return this.letter == other.letter
                && this.color == other.color
                && this.position.equals(other.position);
    }

    public Box executeCommand(Command cmd) {
        // TODO: this might fail with null pointer
        Position newPos = new Position(
                position.getX() - Command.dirToColChange(cmd.dir2),
                position.getY() - Command.dirToRowChange(cmd.dir2));
        return new Box(letter, color, newPos);
    }

    public Box copy() {
        return new Box(letter, color, new Position(position.getX(), position.getY()));
    }

    public Box copy(Position pos) {
        return new Box(letter, color, pos);
    }

    @Override
    public int hashCode() {
        return this.position.hashCode() + this.letter + this.color.ordinal();
    }
}
