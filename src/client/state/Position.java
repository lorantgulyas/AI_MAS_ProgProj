package client.state;

public class Position {
    private int row;
    private int col;

    public Position(int col, int row) {
        this.row = row;
        this.col = col;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    @Override
    public String toString() {
        return "x: " + col + ", y: " + row;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (this.getClass() != obj.getClass())
            return false;
        Position other = (Position) obj;
        return this.row == other.getRow()
                && this.col == other.getCol();
    }

    @Override
    public int hashCode() {
        return this.row + this.col;
    }
}
