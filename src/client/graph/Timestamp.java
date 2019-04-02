package client.graph;

import client.state.Position;

public class Timestamp {

    private Position position;
    private int time;

    public Timestamp(int time, Position position) {
        this.position = position;
        this.time = time;
    }

    public Position getPosition() {
        return position;
    }

    public int getTime() {
        return time;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (obj.getClass() != this.getClass())
            return false;
        Timestamp other = (Timestamp) obj;
        return this.time == other.time && this.position.equals(other.position);
    }

    @Override
    public int hashCode() {
        return this.position.hashCode() * this.time;
    }
}

