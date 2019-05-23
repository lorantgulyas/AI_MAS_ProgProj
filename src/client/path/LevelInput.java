package client.path;

import client.state.Color;
import client.state.Level;
import client.state.Position;
import client.state.State;

public class LevelInput {

    private Position p1;
    private Position p2;
    private Color color;
    private int maxDistance;
    private int _hash;

    public LevelInput(Position p1, Position p2, Color color, int maxDistance) {
        this.p1 = p1;
        this.p2 = p2;
        this.color = color;
        this.maxDistance = maxDistance;
        this._hash = this.computeHashCode();
    }

    public Color getColor() {
        return color;
    }

    public Position getP1() {
        return p1;
    }

    public Position getP2() {
        return p2;
    }

    public int getMaxDistance() {
        return maxDistance;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || obj.getClass() != this.getClass())
            return false;
        LevelInput other = (LevelInput) obj;
        return other.p1.equals(this.p1)
                && other.p2.equals(this.p2)
                && other.color == this.color
                && other.maxDistance == this.maxDistance;
    }

    @Override
    public int hashCode() {
        return this._hash;
    }

    private int computeHashCode() {
        int hashCode = this.color == null ? 1 : (this.color.hashCode() + 1);
        hashCode *= this.p1.hashCode() * this.p2.hashCode() * (this.maxDistance + 1);
        return hashCode;
    }
}
