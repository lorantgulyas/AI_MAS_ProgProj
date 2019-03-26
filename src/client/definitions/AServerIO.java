package client.definitions;

import client.Command;

public abstract class AServerIO {

    protected String name;

    public AServerIO(String name) {
        this.name = name;
    }

    public abstract AState readState() throws Exception;

    public abstract boolean[] sendJointAction(Command[] jointAction)
            throws Exception;

    public abstract void sendComment(String comment);
}
