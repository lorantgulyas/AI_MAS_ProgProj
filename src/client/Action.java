package client;

public class Action {

    private Command command;
    private Timestamp[] timestamps;

    public Action(Command command, Timestamp[] timestamps) {
        this.command = command;
        this.timestamps = timestamps;
    }

    public Command getCommand() {
        return command;
    }

    public Timestamp[] getTimestamps() {
        return timestamps;
    }
}
