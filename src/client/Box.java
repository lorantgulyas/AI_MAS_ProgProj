package client;

//enum Color {
//    blue,
//    red,
//    green
//}

public class Box {
    private char letter;
    private Color color;
    private Position position;

    public Box (char letter, Color color, Position position) {
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
}
