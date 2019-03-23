package client;

public class Goal {
    private char letter;
    private Position location;

    public Goal(char letter, Position location) {
        this.letter = letter;
        this.location = location;
    }

    public char getLetter() {
        return letter;
    }

    public Position getLocation() {
        return location;
    }
}
