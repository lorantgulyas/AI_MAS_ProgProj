package client.state;

public class Box {
    private char letter;
    private Color color;
    private int id;
    private Position position;
    private int _hash;

    /**
     * Constructs a new box.
     *
     * @param letter Letter value of the box.
     * @param color Color of the box.
     * @param position Position of the box.
     * @param id ID of the box. Similarly to agent IDs, this is also the index in the boxes array.
     */
    public Box(int id, char letter, Color color, Position position) {
        this.letter = letter;
        this.color = color;
        this.position = position;
        this.id = id;
        this._hash = this.computeHashCode();
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

    public int getId() {
        return id;
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
        return this.id == other.id
                && this.letter == other.letter
                && this.color == other.color
                && this.position.equals(other.position);
    }

    @Override
    public int hashCode() {
        return this._hash;
    }

    private int computeHashCode() {
        // plus one to avoid multiplying by zero
        return (this.id + 1) * (this.letter + 1) * (this.color.ordinal() + 1) * (this.position.hashCode() + 1);
    }
}
