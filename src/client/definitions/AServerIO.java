package client.definitions;

import client.Command;
import client.State;

public abstract class AServerIO {

    protected String name;

    public AServerIO(String name) {
        this.name = name;
    }

    public abstract State readState();

    public abstract boolean[] sendJointAction(Command[] jointAction);

    public abstract void sendComment(String comment);
}
