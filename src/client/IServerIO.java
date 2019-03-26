package client;

public interface IServerIO {

//    public IServerIO (String name);

    public State readState();

    public boolean[] sendJointAction(Command[] jointAction);

    public void sendComment(String comment);

}
