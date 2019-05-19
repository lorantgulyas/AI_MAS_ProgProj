package client.state;

import client.graph.Command;

public class Position {
    private int row;
    private int col;
    private int _hash;

    public Position(int col, int row) {
        this.row = row;
        this.col = col;
        this._hash = this.computeHashCode();
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public Position go(Command.Dir dir) {
        switch (dir) {
            case E: return this.east();
            case S: return this.south();
            case W: return this.west();
            case N: return this.north();
            default:
                // this case should not be reached
                return null;
        }
    }

    public Position add(Position pos) {
        return new Position(this.col + pos.getCol(), row + pos.getRow());
    }

    public Position north() {
        return new Position(this.col, this.row - 1);
    }

    public Position east() {
        return new Position(this.col + 1, this.row);
    }

    public Position south() {
        return new Position(this.col, this.row + 1);
    }

    public Position west() {
        return new Position(this.col - 1, this.row);
    }

    @Override
    public String toString() {
        return "(" + this.col + "," + this.row + ")";
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
        return this.row == other.getRow() && this.col == other.getCol();
    }

    @Override
    public int hashCode() {
        return this._hash;
    }

    private int computeHashCode() {
        // plus one to avoid multiplying by zero
        return (this.row + 1) * (this.col + 1);
    }
}
