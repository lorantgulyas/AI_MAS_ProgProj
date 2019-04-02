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
}
