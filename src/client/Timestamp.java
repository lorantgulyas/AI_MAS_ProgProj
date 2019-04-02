package client;

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
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Timestamp)) {
            return false;
        }
        Timestamp timestamp = (Timestamp) obj;
        return this.time == timestamp.time && this.position.equals(timestamp.position);
    }

    @Override
    public int hashCode() {
        return this.position.hashCode() + this.time;
    }
}

