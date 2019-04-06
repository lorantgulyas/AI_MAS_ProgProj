package client.state;

public class Goal {
    private char letter;
    private Position position;

    public Goal(char letter, Position position) {
        this.letter = letter;
        this.position = position;
    }

    public char getLetter() {
        return letter;
    }

    public Position getPosition() {
        return position;
    }

    @Override
    public String toString() {
        return "char: " + Character.toLowerCase(letter) + ", position: " + position;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (this.getClass() != obj.getClass())
            return false;
        Goal other = (Goal) obj;
        return this.letter == other.letter
                && this.position.equals(other.position);
    }

    @Override
    public int hashCode() {
        return this.position.hashCode() + this.letter;
    }
}