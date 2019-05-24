package subgoaler;

public class Agent {
    private int id;
    private Color color;
    private Position position;
    private Position goalPosition;

    public Agent(int id, Color color, Position position, Position goalPosition) {
        this.id = id;
        this.color = color;
        this.position = position;
        this.goalPosition = goalPosition;
    }

    public int getId() {
        return id;
    }

    public Color getColor() {
        return color;
    }

    public Position getPosition() {
        return position;
    }

    public Position getGoalPosition() {
        return goalPosition;
    }

    public void setGoalPosition(Position goalPosition) {
        this.goalPosition = goalPosition;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    @Override
    public String toString() {
        return "id: " + id + ", color: " + color + ", position: " + position;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (this.getClass() != obj.getClass())
            return false;
        Agent other = (Agent) obj;
        return this.id == other.id
                && this.color == other.color
                && this.position.equals(other.position);
    }

    public Agent copy() {
        return new Agent(id, color, new Position(position.getX(), position.getY()), goalPosition);
    }

    @Override
    public int hashCode() {
        return this.position.hashCode() + this.id + this.color.ordinal();
    }
}
