package client;

public class Action {

    private Command command;
    private Timestamp timestamp;

    public Action(Command command, Timestamp timestamp) {
        this.command = command;
        this.timestamp = timestamp;
    }

    public Command getCommand() {
        return command;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }
}
