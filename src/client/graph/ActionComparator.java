package client.graph;

import java.util.Comparator;

public class ActionComparator implements Comparator<Action> {

    private CommandComparator commandComparator;

    public ActionComparator() {
        this.commandComparator = new CommandComparator();
    }

    @Override
    public int compare(Action a1, Action a2) {
        Command c1 = a1.getCommand();
        Command c2 = a2.getCommand();
        return this.commandComparator.compare(c1, c2);
    }

}
